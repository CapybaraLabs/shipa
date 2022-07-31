package dev.capybaralabs.shipa.discord.model

import java.time.Instant
import java.util.Optional

/**
 * [Discord Message](https://discord.com/developers/docs/resources/channel#message-object)
 */
data class Message(
	val id: Long,
	val channelId: Long,
	//	author
	val content: String,
	val timestamp: Instant,
	val editedTimestamp: Optional<Instant>,
	val tts: Boolean,
	val mentionEveryone: Boolean,
	val mentions: List<User>,
	val mentionRoles: List<Role>,
	//	mentionChannels
	val attachments: List<Attachment>,
	val embeds: List<Embed>,
	//	reactions
	val nonce: String?, // or Int
	val pinned: Boolean,
	val webhookId: Long?,
	val type: Int, // TODO enum
	//	activity
	//	application
	val applicationId: Long?,
	//	messageReference
	val flags: Int?,
	// TODO more stuff
)
