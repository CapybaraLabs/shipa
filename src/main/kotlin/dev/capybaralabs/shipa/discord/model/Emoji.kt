package dev.capybaralabs.shipa.discord.model

import java.util.Optional

/**
 * [Discord Emoji Object](https://discord.com/developers/docs/resources/emoji#emoji-object)
 */
data class Emoji(
	val id: Optional<Long>,
	val name: Optional<String>,
	val roles: List<Long>?,
	val user: User?,
	val requireColons: Boolean?,
	val managed: Boolean?,
	val animated: Boolean?,
	val available: Boolean?,
)
