package dev.capybaralabs.shipa.discord.model

import java.util.Optional

/**
 * [Discord Attachment](https://discord.com/developers/docs/resources/channel#attachment-object)
 */
data class Attachment(
	val id: Long,
	val filename: String,
	val description: String?,
	val contentType: String?,
	val size: Int,
	val url: String,
	val proxyUrl: String,
	val height: Optional<Int>?,
	val width: Optional<Int>?,
	val ephemeral: Boolean?,
)

/**
 * [Create/Edit Discord Attachments](https://discord.com/developers/docs/resources/channel#attachment-object)
 */
data class PartialAttachment(
	val id: Long,
	val filename: String?,
	val description: String?,
)
