package dev.capybaralabs.shipa.discord.client

import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.ShipaMetrics.Companion.NANOSECONDS_PER_MILLISECOND
import dev.capybaralabs.shipa.discord.client.ratelimit.Bucket
import dev.capybaralabs.shipa.discord.client.ratelimit.BucketKey
import dev.capybaralabs.shipa.discord.client.ratelimit.BucketService
import dev.capybaralabs.shipa.discord.delay
import dev.capybaralabs.shipa.discord.millis
import dev.capybaralabs.shipa.discord.oauth2.OAuth2Scope
import dev.capybaralabs.shipa.discord.oauth2.OAuth2ScopeException
import dev.capybaralabs.shipa.discord.seconds
import dev.capybaralabs.shipa.discord.time
import dev.capybaralabs.shipa.logger
import io.micrometer.core.instrument.Timer
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.http.RequestEntity.UriTemplateRequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestClientResponseException
import tools.jackson.core.JacksonException
import tools.jackson.module.kotlin.jsonMapper

const val HEADER_LIMIT = "X-RateLimit-Limit"
const val HEADER_REMAINING = "X-RateLimit-Remaining"
const val HEADER_RESET = "X-RateLimit-Reset"
const val HEADER_RESET_AFTER = "X-RateLimit-Reset-After"
const val HEADER_BUCKET = "X-RateLimit-Bucket"
const val HEADER_GLOBAL = "X-RateLimit-Global"
const val HEADER_SCOPE = "X-RateLimit-Scope"
const val HEADER_RETRY_AFTER = "Retry-After"


class DiscordRestService(
	private val authToken: DiscordAuthToken,
	private val restTemplateBuilder: RestTemplateBuilder,
	private val bucketService: BucketService,
	private val metrics: ShipaMetrics,
) {

	private val mapper = jsonMapper()
	private val restTemplate = restTemplateBuilder.additionalInterceptors(
		{ req, body, exec ->
			req.headers.add(HttpHeaders.AUTHORIZATION, authToken.authHeader())
			exec.execute(req, body)
		},
	).build()

	@OptIn(ExperimentalCoroutinesApi::class)
	private val restDispatcher = Dispatchers.IO.limitedParallelism(100)

	fun withUser(authToken: DiscordAuthToken.Oauth2): DiscordRestService {
		return DiscordRestService(authToken, restTemplateBuilder, bucketService, metrics)
	}

	/**
	 * @throws OAuth2ScopeException if the token is a user token and is missing the required scope
	 */
	fun assertUserHasScope(scope: OAuth2Scope) {
		when (authToken) {
			is DiscordAuthToken.Bot -> {}  // is this a sane assumption? how do we know the bot can access the concrete endpoint?
			is DiscordAuthToken.Oauth2 -> {
				if (!authToken.scopes.contains(scope)) {
					throw OAuth2ScopeException(authToken, scope)
				}
			}
		}
	}

	/**
	 * [Discord Rate Limits](https://discord.com/developers/docs/topics/rate-limits#rate-limits)
	 *
	 * Bucket Keys are top-level resources. Currently considered:
	 *  - guildId
	 *  - channelId
	 *  - webhookId
	 *  - webhookId + token
	 *
	 * The applicationId should also be included, especially in case multiple applications are used.
	 *
	 * If the RequestEntity was constructed using a complex URI builder, provide a generic URI template to overide it
	 * for tracking, otherwise logs and metrics will contain concrete parameters, leading to high cardinality.
	 *
	 * Automatically retries eligible requests if they fail due to rate limiting, transport or server errors.
	 * Logical Discord errors are thrown immediately as [DiscordClientException].
	 * Other errors are wrapped in [DiscordClientRequestFailedException].
	 */
	@Throws(DiscordClientException::class)
	suspend inline fun <reified R : Any> exchange(
		bucketKey: BucketKey,
		request: RequestEntity<*>,
		uriTemplateOverride: String? = null,
		retryNotFoundTimes: Int = 0,
		retryErrorTimes: Int = 3,
	): ResponseEntity<R> {
		return exchange(
			bucketKey,
			request,
			object : ParameterizedTypeReference<R>() {},
			uriTemplateOverride,
			retryNotFoundTimes,
			retryErrorTimes,
		)
	}

	@Throws(DiscordClientException::class)
	suspend fun <R : Any> exchange(
		bucketKey: BucketKey,
		request: RequestEntity<*>,
		type: ParameterizedTypeReference<R>,
		uriTemplateOverride: String? = null,
		retryNotFoundTimes: Int = 0,
		retryErrorTimes: Int = 3,
	): ResponseEntity<R> {
		val bucket = bucketService.bucket(authToken, bucketKey)
		bucket.mutex.withLock {
			try {
				var notFoundTry = 0
				val errors: MutableList<Exception> = mutableListOf()
				while (true) { // consider escape hatch after X attempts or reaching a max attempt duration
					if (bucket.tokens <= 0) {
						val untilReset = Duration.between(Instant.now(), bucket.nextReset)
						if (!untilReset.isNegative) {
							delay(untilReset)
						}
					}

					val uriTemplate = uriTemplateOverride ?: detectUriTemplate(request)
					val requestResult: DiscordRequestResult<R> = withContext(restDispatcher) {
						instrumentRequest(request, uriTemplate) { restTemplate.exchange(request, type) }
					}
					when (requestResult) {
						is DiscordRequestResult.DiscordRequestResultError -> {
							val exception = requestResult.exception
							when (requestResult) {
								is DiscordRequestResult.TransportError -> {} // no response, fall through to retry logic
								is DiscordRequestResult.ServerError -> {
									// only fall through if it's a 5xx error
									if (!requestResult.exception.statusCode.is5xxServerError) {
										logger().warn("Got non-5xx other error, not retrying request, body {}", requestResult.exception.responseBodyAsString, exception)
										throw DiscordClientRequestFailedException(exception, *errors.toTypedArray())
									}
								}

								is DiscordRequestResult.LogicalError -> {
									val discordClientException = requestResult.exception
									val cause = discordClientException.cause
									val resetAfter = cause.responseHeaders?.let {
										updateBucket(bucket, it, uriTemplate)
										resetAfter(it)
									}

									if (cause is NotFound && notFoundTry < retryNotFoundTimes) {
										// retry. could be race condition where the ack response has not been processed yet
										logger().info("Got 404, retrying #$notFoundTry")
										notFoundTry++
										delay(500.millis)
										continue
									}

									if (cause is TooManyRequests) {
										logger().info("Hit ratelimit on bucket {}: {}", bucketKey, cause.message)
										if (resetAfter == null) {
											logger().warn("Hit ratelimit on bucket {} but no known wait time. Backing off.", bucketKey, cause)
											delay(1.seconds) // shrug
										} else {
											delay(resetAfter)
										}
										continue
									}

									//do not retry Discord Errors, they are logical errors, not transport/server error
									throw discordClientException
								}
							}

							// retry logic here
							if (errors.size < retryErrorTimes) {
								logger().info("Got error, retrying request #{}", errors.size, exception)
								errors.add(exception)
								delay(500.millis) // shrug
								continue
							}

							throw DiscordClientRequestFailedException(exception, *errors.toTypedArray())
						}

						is DiscordRequestResult.ResponseSuccess -> {
							updateBucket(bucket, requestResult.response.headers, uriTemplate)
							return requestResult.response
						}
					}
				}
			} finally {
				bucketService.update(authToken, bucketKey, bucket) // trigger expiry update
			}
		}
	}

	private fun detectUriTemplate(request: RequestEntity<*>): String {
		return if (request is UriTemplateRequestEntity) request.uriTemplate else "Not Template"
	}

	//
	// possible outcomes for requests to discord:
	// 1. no response / hard error
	// 2. response
	// 	2a. discord error
	// 	2b. other error (e.g. some 5xxs, cloudflare bullshit, etc)
	// 	2c. success
	//
	private sealed interface DiscordRequestResult<R> {

		sealed interface DiscordRequestResultError<R> : DiscordRequestResult<R> {
			val exception: Exception
		}

		// likely transport error. we never received a response.
		data class TransportError<R>(override val exception: Exception) : DiscordRequestResultError<R>

		// some error we received as a response. 5xx, cloudflare, etc. could be a transport error, could be a general server error
		data class ServerError<R>(override val exception: RestClientResponseException) : DiscordRequestResultError<R>

		// logical discord error. we are rate limited, missing permissions, etc.
		data class LogicalError<R>(override val exception: DiscordClientLogicalException) : DiscordRequestResultError<R>

		// success. we got a response, and Discord is happy as well.
		data class ResponseSuccess<R : Any>(val response: ResponseEntity<R>) : DiscordRequestResult<R>
	}


	private fun <R : Any> instrumentRequest(request: RequestEntity<*>, uriTemplate: String, block: () -> ResponseEntity<R>): DiscordRequestResult<R> {
		val method = request.method?.name() ?: "WTF"

		val timer = Timer.start()
		try {
			val result = metrics.discordRestRequestResponseTime().time(block)

			val responseTimeNanos = timer.stop(metrics.discordRestRequests(method, uriTemplate, "${result.statusCode.value()}", ""))
			val responseTimeMillis = (responseTimeNanos / NANOSECONDS_PER_MILLISECOND).toInt()

			logger().debug("{} {} {}ms {}", method, uriTemplate, responseTimeMillis, result.statusCode.value())

			return DiscordRequestResult.ResponseSuccess(result)
		} catch (e: RestClientResponseException) {
			// see https://discord.com/developers/docs/topics/opcodes-and-status-codes#http-http-response-codes
			val responseBody = e.responseBodyAsString
			if (e.statusCode.is5xxServerError) {
				timer.stop(metrics.discordRestRequests(method, uriTemplate, "${e.statusCode.value()}", ""))
				logger().warn("Failed request to: {} {} with response {}", method, uriTemplate, responseBody, e)
				return DiscordRequestResult.ServerError(e)
			}

			// https://discord.com/developers/docs/reference#error-messages
			val tree = try {
				mapper.readTree(responseBody)
			} catch (j: JacksonException) {
				logger().warn("Failed to parse discords error response: {}", responseBody, j)
				timer.stop(metrics.discordRestRequests(method, uriTemplate, "${e.statusCode.value()}", ""))
				logger().warn("Failed request to: {} {} with response {}", method, uriTemplate, responseBody, e)
				return DiscordRequestResult.ServerError(e)
			}
			val errorCode = tree.get("code")?.asInt(-1) ?: -1

			val responseTimeNanos = timer.stop(metrics.discordRestRequests(method, uriTemplate, "${e.statusCode.value()}", "$errorCode"))

			val message = tree.get("message")?.asString()
			val errors = tree.get("errors")?.asString()
			val responseTimeMillis = (responseTimeNanos / NANOSECONDS_PER_MILLISECOND).toInt()
			logger().debug("Encountered error response: {} {} {}ms {} {} {} {}", method, uriTemplate, responseTimeMillis, e.statusCode.value(), errorCode, message, errors)

			return DiscordRequestResult.LogicalError(DiscordClientLogicalException(JsonErrorCode.parse(errorCode), message, errors, e))
		} catch (e: Exception) {
			metrics.discordRestHardFailures(method, uriTemplate).increment()
			logger().warn("Failed request to: {} {}", method, uriTemplate, e)
			return DiscordRequestResult.TransportError(e)
		}
	}

	private fun updateBucket(bucket: Bucket, responseHeaders: HttpHeaders, uriTemplate: String) {
		val limit = responseHeaders.getFirst(HEADER_LIMIT)?.toInt()
		val remaining = responseHeaders.getFirst(HEADER_REMAINING)?.toInt()
		val resetAfter = resetAfter(responseHeaders)

		if (limit != null) bucket.limit = limit
		if (remaining != null) bucket.tokens = remaining else bucket.tokens -= 1
		if (resetAfter != null) bucket.nextReset = Instant.now().plus(resetAfter)


		val name = discordBucketName(responseHeaders, uriTemplate)
		logger().debug("Got Bucket {} with {} {} {} on route {}", name, limit, remaining, resetAfter, uriTemplate)
		if (bucket.discordName != null && bucket.discordName != name) {
			logger().warn(
				"Got different discord bucket names {} -> {} on route {}. Could indicate a problem with bucket key determination.",
				bucket.discordName, name, uriTemplate,
			)
		}
		bucket.discordName = name
	}

	private fun discordBucketName(responseHeaders: HttpHeaders, uriTemplate: String): String {
		val isGlobal = responseHeaders.getFirst(HEADER_GLOBAL) == "true"
			|| responseHeaders.getFirst(HEADER_SCOPE) == "global"

		if (isGlobal) {
			return "global"
		}
		val header = responseHeaders.getFirst(HEADER_BUCKET)
		if (header != null) {
			return header
		}
		logger().warn("Unknown bucket header on request: {}, headers: {}", uriTemplate, responseHeaders)
		return "unknown"
	}

	private fun resetAfter(responseHeaders: HttpHeaders): Duration? {

		val resetAfterHeader: String? = (responseHeaders.getFirst(HEADER_RESET_AFTER)
			?: responseHeaders.getFirst(HEADER_RETRY_AFTER))

		return resetAfterHeader?.toDouble()?.times(1000)?.millis
	}
}
