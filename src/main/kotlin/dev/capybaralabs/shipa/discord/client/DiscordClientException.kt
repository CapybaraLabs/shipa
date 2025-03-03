package dev.capybaralabs.shipa.discord.client

sealed class DiscordClientException(
	message: String? = null,
	cause: Exception? = null,
) : RuntimeException(message, cause)
