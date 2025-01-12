package dev.capybaralabs.shipa.discord.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.http.RequestEntity.UriTemplateRequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

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

	private val mapper = ObjectMapper()
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
	 */
	suspend inline fun <reified R> exchange(
		bucketKey: BucketKey,
		request: RequestEntity<*>,
		uriTemplateOverride: String? = null,
		retryNotFoundTimes: Int = 0,
	): ResponseEntity<R> {
		return exchange(bucketKey, request, object : ParameterizedTypeReference<R>() {}, uriTemplateOverride, retryNotFoundTimes)
	}

	suspend fun <R> exchange(
		bucketKey: BucketKey,
		request: RequestEntity<*>,
		type: ParameterizedTypeReference<R>,
		uriTemplateOverride: String? = null,
		retryNotFoundTimes: Int = 0,
	): ResponseEntity<R> {
		val bucket = bucketService.bucket(authToken, bucketKey)
		bucket.mutex.withLock {
			try {
				var notFoundTry = 0
				while (true) { // consider escape hatch after X attempts or reaching a max attempt duration
					if (bucket.tokens <= 0) {
						val untilReset = Duration.between(Instant.now(), bucket.nextReset)
						if (!untilReset.isNegative) {
							delay(untilReset)
						}
					}

					val response: ResponseEntity<R>
					val uriTemplate = uriTemplateOverride ?: detectUriTemplate(request)
					try {
						response = withContext(restDispatcher) {
							instrument(request, uriTemplate) { restTemplate.exchange(request, type) }
						}
					} catch (discordClientException: DiscordClientException) {
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


						if (cause !is TooManyRequests) {
							throw discordClientException
						}

						logger().info("Hit ratelimit on bucket {}: {}", bucketKey, cause.message)
						if (resetAfter == null) {
							logger().warn("Hit ratelimit on bucket {} but no known wait time. Backing off.", bucketKey, cause)
							delay(1.seconds) // shrug
						} else {
							delay(resetAfter)
						}
						continue
					}

					updateBucket(bucket, response.headers, uriTemplate)
					return response
				}
			} finally {
				bucketService.update(authToken, bucketKey, bucket) // trigger expiry update
			}
		}
	}

	private fun detectUriTemplate(request: RequestEntity<*>): String {
		return if (request is UriTemplateRequestEntity) request.uriTemplate else "Not Template"
	}

	private fun <R> instrument(request: RequestEntity<*>, uriTemplate: String, block: () -> ResponseEntity<R>): ResponseEntity<R> {
		val method = request.method?.name() ?: "WTF"

		val timer = Timer.start()
		try {
			val result = metrics.discordRestRequestResponseTime().time(block)

			val responseTimeNanos = timer.stop(metrics.discordRestRequests(method, uriTemplate, "${result.statusCode.value()}", ""))
			val responseTimeMillis = (responseTimeNanos / NANOSECONDS_PER_MILLISECOND).toInt()

			logger().debug("{} {} {}ms {}", method, uriTemplate, responseTimeMillis, result.statusCode.value())

			return result
		} catch (e: RestClientResponseException) {
			// https://discord.com/developers/docs/reference#error-messages
			val tree = try {
				mapper.readTree(e.responseBodyAsString)
			} catch (e: JsonProcessingException) {
				throw hardRestFail(e, method, uriTemplate)
			}
			val errorCode = tree.get("code")?.asInt(-1) ?: -1

			val responseTimeNanos = timer.stop(metrics.discordRestRequests(method, uriTemplate, "${e.statusCode.value()}", "$errorCode"))

			val message = tree.get("message")?.asText()
			val errors = tree.get("errors")?.asText()
			val responseTimeMillis = (responseTimeNanos / NANOSECONDS_PER_MILLISECOND).toInt()
			logger().debug("Encountered error response: {} {} {}ms {} {} {} {}", method, uriTemplate, responseTimeMillis, e.statusCode.value(), errorCode, message, errors)

			throw DiscordClientException(JsonErrorCode.parse(errorCode), message, errors, e)
		} catch (e: RestClientException) {
			throw hardRestFail(e, method, uriTemplate)
		}
	}

	private fun hardRestFail(e: Exception, method: String, uriTemplate: String): Exception {
		metrics.discordRestHardFailures(method, uriTemplate).increment()
		logger().warn("Failed request to: {} {}", method, uriTemplate, e)
		return e
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
