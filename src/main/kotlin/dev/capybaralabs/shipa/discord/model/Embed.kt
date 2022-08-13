package dev.capybaralabs.shipa.discord.model

import java.time.Instant

/**
 * [Discord Embed](https://discord.com/developers/docs/resources/channel#embed-object)
 */
data class Embed(
	val title: String? = null,
	val type: String? = null,
	val description: String? = null,
	val url: String? = null,
	val timestamp: Instant? = null,
	val color: Int? = null,
	val footer: EmbedFooter? = null,
	val image: EmbedImage? = null,
	val thumbnail: EmbedThumbnail? = null,
	val video: EmbedVideo? = null,
	val provider: EmbedProvider? = null,
	val author: EmbedAuthor? = null,
	val fields: List<EmbedField>? = null,
)

data class EmbedFooter(
	val text: String,
	val iconUrl: String? = null,
	val proxyIconUrl: String? = null,
)

data class EmbedImage(
	val url: String,
	val proxyUrl: String? = null,
	val height: Int? = null,
	val width: Int? = null,
)

data class EmbedThumbnail(
	val url: String,
	val proxyUrl: String? = null,
	val height: Int? = null,
	val width: Int? = null,
)

data class EmbedVideo(
	val url: String? = null,
	val proxyUrl: String? = null,
	val height: Int? = null,
	val width: Int? = null,
)

data class EmbedProvider(
	val name: String? = null,
	val url: String? = null,
)

data class EmbedAuthor(
	val name: String,
	val url: String? = null,
	val iconUrl: String? = null,
	val proxyIconUrl: String? = null,
)

const val ZERO_WIDTH_SPACE = "\u200E"

data class EmbedField private constructor(
	val name: String = ZERO_WIDTH_SPACE,
	val value: String = ZERO_WIDTH_SPACE,
	val inline: Boolean? = null,
) {
	companion object {
		operator fun invoke(name: String? = null, value: String? = null, inline: Boolean? = null): EmbedField {
			return EmbedField(
				(name ?: ZERO_WIDTH_SPACE).let { it.ifBlank { ZERO_WIDTH_SPACE } },
				(value ?: ZERO_WIDTH_SPACE).let { it.ifBlank { ZERO_WIDTH_SPACE } },
				inline
			)
		}
	}
}
