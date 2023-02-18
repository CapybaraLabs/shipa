package dev.capybaralabs.shipa.discord.model

import dev.capybaralabs.shipa.discord.model.ImageFormatting.Format.GIF
import dev.capybaralabs.shipa.discord.model.ImageFormatting.Format.PNG
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

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
) {

	fun emojiUrl(): String? {
		return id.getOrNull()?.let {
			val format = if (animated == true) GIF else PNG
			ImageFormatting.imageUrl("/emojis/$it", format)
		}
	}

}
