package dev.capybaralabs.shipa.discord.interaction.validation

interface InteractionValidator {
	/**
	 * See https://discord.com/developers/docs/interactions/slash-commands#security-and-authorization
	 *
	 * @param signature the signature header of the request
	 * @param timestamp the timestamp header of the request
	 * @param body      the body of the request
	 * @return `true` if the request is valid, `false` otherwise
	 */
	fun validateSignature(signature: String, timestamp: String, body: String): Boolean
}
