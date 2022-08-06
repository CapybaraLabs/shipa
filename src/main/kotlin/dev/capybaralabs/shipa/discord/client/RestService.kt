package dev.capybaralabs.shipa.discord.client

import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

const val HEADER_LIMIT = "X-RateLimit-Limit"
const val HEADER_REMAINING = "X-RateLimit-Remaining"
const val HEADER_RESET = "X-RateLimit-Reset"
const val HEADER_RESET_AFTER = "X-RateLimit-Reset-After"
const val HEADER_BUCKET = "X-RateLimit-Bucket"
const val HEADER_GLOBAL = "X-RateLimit-Global"
const val HEADER_SCOPE = "X-RateLimit-Scope"
const val RETRY_AFTER = "Retry-After"

@Service
class RestService(
	val restTemplate: RestTemplate,
) {

	final suspend inline fun <reified R> exchange(request: RequestEntity<*>): ResponseEntity<R> {
		val response = restTemplate.exchange<R>(request)
		if (response.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
			// TODO ratelimit handling
		}
		return response
	}

}
