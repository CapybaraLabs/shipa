package dev.capybaralabs.shipa.discord.client

/**
 * The 1-n underlying exceptions that caused a request to hard-fail.
 * There can be more than one because the client may retry the request.
 * The last one is generally treated as the main "cause", while the others are treated as "suppressed".
 *
 * Soft / logical failures are represented by [DiscordClientLogicalException].
 */
class DiscordClientRequestFailedException(
	override val cause: Exception,
	@Suppress("MemberVisibilityCanBePrivate") // intended as public API
	vararg val suppressed: Exception,
) : DiscordClientException(cause = cause) {

	init {
		suppressed.forEach(::addSuppressed)
	}
}
