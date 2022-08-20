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
	val flags: IntBitfield<MessageFlag>?,
	// TODO more stuff
)

enum class MessageFlag(override val value: Int) : IntBitflag {
	CROSSPOSTED(1 shl 0),
	IS_CROSSPOST(1 shl 1),
	SUPPRESS_EMBEDS(1 shl 2),
	SOURCE_MESSAGE_DELETED(1 shl 3),
	URGENT(1 shl 4),
	HAS_THREAD(1 shl 5),
	EPHEMERAL(1 shl 6),
	LOADING(1 shl 7),
	FAILED_TO_MENTION_SOME_ROLES_IN_THREAD(1 shl 8),
}
