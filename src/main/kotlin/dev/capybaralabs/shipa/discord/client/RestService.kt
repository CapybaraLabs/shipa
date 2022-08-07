@file:Suppress("UastIncorrectHttpHeaderInspection")

package dev.capybaralabs.shipa.discord.client

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
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.RequestEntity.UriTemplateRequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
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
) {

	final suspend inline fun <reified R> exchange(request: RequestEntity<*>): ResponseEntity<R> {
		return exchange(request, object : ParameterizedTypeReference<R>() {})
	}

	suspend fun <R> exchange(request: RequestEntity<*>, type: ParameterizedTypeReference<R>): ResponseEntity<R> {

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

					val response = withContext(Dispatchers.IO) {
						restTemplate.exchange(request, type)
					}

					updateBucket(bucket, response)

					if (response.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
						logger().warn("Hit ratelimit!")
						val resetAfter = resetAfter(response)
						if (resetAfter == null) {
							logger().warn("Hit ratelimit but don't know how long to wait")
							delay(1.seconds) // shrug
						} else {
							delay(resetAfter.toKotlinDuration())
						}
						continue
					}

					return response
				}
			} finally {
				bucketService.update(bucketKey, bucket) // trigger expiry update
			}
		}
	}

	private fun updateBucket(bucket: Bucket, response: ResponseEntity<*>) {
		val limit = response.headers.getFirst(HEADER_LIMIT)?.toInt()
		val remaining = response.headers.getFirst(HEADER_REMAINING)?.toInt()
		val resetAfter = resetAfter(response)

		if (limit != null) bucket.limit = limit
		if (remaining != null) bucket.tokens = remaining else bucket.tokens -= 1
		if (resetAfter != null) bucket.nextReset = Instant.now().plus(resetAfter)


		val name = discordBucketName(response)
		logger().debug("Got Bucket $name with $limit $remaining $resetAfter")
		if (bucket.discordName != null && bucket.discordName != name) {
			logger().warn("Got different discord bucket names ${bucket.discordName} -> $name. Could indicate a problem with bucket key determination.")
		}
		bucket.discordName = name
	}

	private fun discordBucketName(response: ResponseEntity<*>): String {
		val isGlobal = response.headers.getFirst(HEADER_GLOBAL) == "true"
			|| response.headers.getFirst(HEADER_SCOPE) == "global"

		return if (isGlobal) {
			"global"
		} else {
			response.headers.getFirst(HEADER_BUCKET) ?: "unknown"
		}
	}

	private fun resetAfter(response: ResponseEntity<*>): Duration? {

		val resetAfterHeader: String? = (response.headers.getFirst(HEADER_RESET_AFTER)
			?: response.headers.getFirst(HEADER_RETRY_AFTER))

		return resetAfterHeader?.toDouble()?.times(1000)?.milliseconds?.toJavaDuration()
	}
}
