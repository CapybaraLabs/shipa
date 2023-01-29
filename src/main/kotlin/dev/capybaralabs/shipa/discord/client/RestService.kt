@file:Suppress("UastIncorrectHttpHeaderInspection")

package dev.capybaralabs.shipa.discord.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.discord.client.ratelimit.Bucket
import dev.capybaralabs.shipa.discord.client.ratelimit.BucketService
import dev.capybaralabs.shipa.logger
import io.prometheus.client.Collector
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.http.RequestEntity.UriTemplateRequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

const val HEADER_LIMIT = "X-RateLimit-Limit"
const val HEADER_REMAINING = "X-RateLimit-Remaining"
const val HEADER_RESET = "X-RateLimit-Reset"
const val HEADER_RESET_AFTER = "X-RateLimit-Reset-After"
const val HEADER_BUCKET = "X-RateLimit-Bucket"
const val HEADER_GLOBAL = "X-RateLimit-Global"
const val HEADER_SCOPE = "X-RateLimit-Scope"
const val HEADER_RETRY_AFTER = "Retry-After"


class RestService(
	private val restTemplate: RestTemplate,
	private val bucketService: BucketService,
	private val metrics: ShipaMetrics,
) {

	private val mapper = ObjectMapper()

	@OptIn(ExperimentalCoroutinesApi::class)
	private val restDispatcher = Dispatchers.IO.limitedParallelism(100)

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
	 */
	final suspend inline fun <reified R> exchange(bucketKey: String, request: RequestEntity<*>): ResponseEntity<R> {
		return exchange(bucketKey, request, object : ParameterizedTypeReference<R>() {})
	}

	suspend fun <R> exchange(bucketKey: String, request: RequestEntity<*>, type: ParameterizedTypeReference<R>): ResponseEntity<R> {
		val bucket = bucketService.bucket(bucketKey)
		bucket.mutex.withLock {
			try {
				while (true) { // consider escape hatch after X attempts or reaching a max attempt duration
					val untilReset = Duration.between(Instant.now(), bucket.nextReset)
					if (bucket.tokens <= 0 && !untilReset.isNegative) {
						delay(untilReset.toKotlinDuration())
					}

					val response: ResponseEntity<R>
					try {
						response = withContext(restDispatcher) {
							instrument(request) { restTemplate.exchange(request, type) }
						}
					} catch (discordClientException: DiscordClientException) {
						val cause = discordClientException.cause
						if (cause !is TooManyRequests) {
							throw discordClientException
						}

						logger().info("Hit ratelimit on bucket $bucketKey: ${cause.message}")
						val resetAfter = cause.responseHeaders?.let {
							updateBucket(bucket, it)
							resetAfter(it)
						}
						if (resetAfter == null) {
							logger().warn("Hit ratelimit on bucket $bucketKey but no known wait time. Backing off.", cause)
							delay(1.seconds) // shrug
						}
						continue
					}

					updateBucket(bucket, response.headers)
					return response
				}
			} finally {
				bucketService.update(bucketKey, bucket) // trigger expiry update
			}
		}
	}

	private fun <R> instrument(request: RequestEntity<*>, block: () -> ResponseEntity<R>): ResponseEntity<R> {
		val method = request.method?.name() ?: "WTF"
		val uri = if (request is UriTemplateRequestEntity) request.uriTemplate else "Not Template"

		val started = System.nanoTime()
		try {
			val result = metrics.discordRestRequestResponseTime.startTimer().use { block.invoke() }

			val responseTimeNanos = System.nanoTime() - started
			val responseTimeSeconds: Double = responseTimeNanos / Collector.NANOSECONDS_PER_SECOND

			metrics.discordRestRequests
				.labels(method, uri, "${result.statusCode.value()}", "")
				.observe(responseTimeSeconds)

			val responseTimeMillis = (responseTimeSeconds * 1000).toInt()
			logger().debug("$method $uri ${responseTimeMillis}ms ${result.statusCode.value()}")

			return result
		} catch (e: RestClientResponseException) {
			val responseTimeNanos = System.nanoTime() - started
			val responseTimeSeconds: Double = responseTimeNanos / Collector.NANOSECONDS_PER_SECOND

			// https://discord.com/developers/docs/reference#error-messages
			val tree = try {
				mapper.readTree(e.responseBodyAsString)
			} catch (e: JsonProcessingException) {
				throw hardRestFail(e, method, uri)
			}
			val errorCode = tree.get("code")?.asInt(-1) ?: -1

			metrics.discordRestRequests
				.labels(method, uri, "${e.statusCode.value()}", "$errorCode")
				.observe(responseTimeSeconds)

			val message = tree.get("message")?.asText()
			val errors = tree.get("errors")?.asText()
			val responseTimeMillis = (responseTimeSeconds * 1000).toInt()
			logger().debug("Encountered error response: $method $uri ${responseTimeMillis}ms ${e.statusCode.value()} $errorCode $message $errors")

			throw DiscordClientException(JsonErrorCode.parse(errorCode), message, errors, e)
		} catch (e: RestClientException) {
			throw hardRestFail(e, method, uri)
		}
	}

	private fun hardRestFail(e: Exception, method: String, uri: String): Exception {
		metrics.discordRestHardFailures.labels(method, uri).inc()
		logger().warn("Failed request to: $method $uri", e)
		return e
	}

	private fun updateBucket(bucket: Bucket, responseHeaders: HttpHeaders) {
		val limit = responseHeaders.getFirst(HEADER_LIMIT)?.toInt()
		val remaining = responseHeaders.getFirst(HEADER_REMAINING)?.toInt()
		val resetAfter = resetAfter(responseHeaders)

		if (limit != null) bucket.limit = limit
		if (remaining != null) bucket.tokens = remaining else bucket.tokens -= 1
		if (resetAfter != null) bucket.nextReset = Instant.now().plus(resetAfter)


		val name = discordBucketName(responseHeaders)
		logger().debug("Got Bucket $name with $limit $remaining $resetAfter")
		if (bucket.discordName != null && bucket.discordName != name) {
			logger().warn("Got different discord bucket names ${bucket.discordName} -> $name. Could indicate a problem with bucket key determination.")
		}
		bucket.discordName = name
	}

	private fun discordBucketName(responseHeaders: HttpHeaders): String {
		val isGlobal = responseHeaders.getFirst(HEADER_GLOBAL) == "true"
			|| responseHeaders.getFirst(HEADER_SCOPE) == "global"

		return if (isGlobal) "global" else responseHeaders.getFirst(HEADER_BUCKET) ?: "unknown"
	}

	private fun resetAfter(responseHeaders: HttpHeaders): Duration? {

		val resetAfterHeader: String? = (responseHeaders.getFirst(HEADER_RESET_AFTER)
			?: responseHeaders.getFirst(HEADER_RETRY_AFTER))

		return resetAfterHeader?.toDouble()?.times(1000)?.milliseconds?.toJavaDuration()
	}
}
