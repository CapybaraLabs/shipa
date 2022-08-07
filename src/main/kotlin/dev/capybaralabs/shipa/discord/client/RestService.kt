@file:Suppress("UastIncorrectHttpHeaderInspection")

package dev.capybaralabs.shipa.discord.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.discord.client.ratelimit.Bucket
import dev.capybaralabs.shipa.discord.client.ratelimit.BucketService
import dev.capybaralabs.shipa.logger
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.http.RequestEntity.UriTemplateRequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.TooManyRequests
import org.springframework.web.client.RestTemplate

const val HEADER_LIMIT = "X-RateLimit-Limit"
const val HEADER_REMAINING = "X-RateLimit-Remaining"
const val HEADER_RESET = "X-RateLimit-Reset"
const val HEADER_RESET_AFTER = "X-RateLimit-Reset-After"
const val HEADER_BUCKET = "X-RateLimit-Bucket"
const val HEADER_GLOBAL = "X-RateLimit-Global"
const val HEADER_SCOPE = "X-RateLimit-Scope"
const val HEADER_RETRY_AFTER = "Retry-After"


@Service
class RestService(
	private val restTemplate: RestTemplate,
	private val bucketService: BucketService,
	@Suppress("SpringJavaInjectionPointsAutowiringInspection") private val mapper: ObjectMapper,
) {

	final suspend inline fun <reified R> exchange(request: RequestEntity<*>): R {
		return exchange(request, object : TypeReference<R>() {})
	}

	suspend fun <R> exchange(request: RequestEntity<*>, type: TypeReference<R>): R {

		val bucketKey = if (request is UriTemplateRequestEntity) {
			request.uriTemplate
		} else {
			logger().warn("$request is not a UriTemplateRequestEntity so cannot determine bucket key, using a shared one.")
			"unknown"
		}

		val bucket = bucketService.bucket(bucketKey)
		bucket.mutex.withLock {
			try {
				while (true) { // consider escape hatch after X attempts or reaching a max attempt duration
					val untilReset = Duration.between(Instant.now(), bucket.nextReset)
					if (bucket.tokens <= 0 && !untilReset.isNegative) {
						delay(untilReset.toKotlinDuration())
					}

					val response: ResponseEntity<String>
					try {
						response = withContext(Dispatchers.IO) {
							restTemplate.exchange(request, String::class.java)
						}
					} catch (e: TooManyRequests) {
						logger().warn("Hit ratelimit!", e)
						val resetAfter = e.responseHeaders?.let {
							updateBucket(bucket, it)
							resetAfter(it)
						}
						if (resetAfter != null) {
							delay(resetAfter.toKotlinDuration())
						} else {
							logger().warn("Hit ratelimit but don't know how long to wait")
							delay(1.seconds) // shrug
						}
						continue
					}

					updateBucket(bucket, response.headers)
					val body = response.body
					return mapper.readValue(body, type)
				}
			} finally {
				bucketService.update(bucketKey, bucket) // trigger expiry update
			}
		}
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
