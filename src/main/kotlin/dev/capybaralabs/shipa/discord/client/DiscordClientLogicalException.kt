package dev.capybaralabs.shipa.discord.client

import org.springframework.http.HttpStatusCode
import org.springframework.web.client.RestClientResponseException

/**
 * Wraps the Discord [JsonErrorCode] and the [RestClientResponseException] that caused the logical request failure.
 *
 * Hard failures are represented by [DiscordClientRequestFailedException].
 */
@Suppress("unused") // intended as public API
class DiscordClientLogicalException(
	val code: JsonErrorCode,
	message: String?,
	/**
	 * See [Discord Error Messages](https://discord.com/developers/docs/reference#error-messages)
	 */
	val errors: String?,
	override val cause: RestClientResponseException,
) : DiscordClientException(message, cause) {

	val httpStatusCode: HttpStatusCode
		get() = cause.statusCode
}
