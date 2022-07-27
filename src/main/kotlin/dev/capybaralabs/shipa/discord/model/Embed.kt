package dev.capybaralabs.shipa.discord.model

import java.time.Instant

/**
 * [Discord Embed](https://discord.com/developers/docs/resources/channel#embed-object)
 */
data class Embed(
	val title: String?,
	val type: String?,
	val description: String?,
	val url: String?,
	val timestamp: Instant?,
	val color: Int?,
	val footer: EmbedFooter?,
	val image: EmbedImage?,
	val thumbnail: EmbedThumbnail?,
	val video: EmbedVideo?,
	val provider: EmbedProvider?,
	val author: EmbedAuthor?,
	val fields: List<EmbedField>?,
)

data class EmbedFooter(
	val text: String,
	val iconUrl: String?,
	val proxyIconUrl: String?,
)

data class EmbedImage(
	val url: String,
	val proxyUrl: String?,
	val height: Int?,
	val width: Int?,
)

data class EmbedThumbnail(
	val url: String,
	val proxyUrl: String?,
	val height: Int?,
	val width: Int?,
)

data class EmbedVideo(
	val url: String?,
	val proxyUrl: String?,
	val height: Int?,
	val width: Int?,
)

data class EmbedProvider(
	val name: String?,
	val url: String?,
)

data class EmbedAuthor(
	val name: String,
	val url: String?,
	val iconUrl: String?,
	val proxyIconUrl: String?,
)

data class EmbedField(
	val name: String,
	val value: String,
	val inline: Boolean?,
)
