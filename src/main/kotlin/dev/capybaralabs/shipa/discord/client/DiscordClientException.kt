package dev.capybaralabs.shipa.discord.client

import org.springframework.http.HttpStatusCode
import org.springframework.web.client.RestClientResponseException

class DiscordClientException(
	val code: JsonErrorCode,
	message: String?,
	/**
	 * See [Discord Error Messages](https://discord.com/developers/docs/reference#error-messages)
	 */
	val errors: String?,
	override val cause: RestClientResponseException,
) : RuntimeException(message, cause) {

	val httpStatusCode: HttpStatusCode
		get() = cause.statusCode
}
