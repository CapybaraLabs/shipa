package dev.capybaralabs.shipa.discord.interaction.model

import dev.capybaralabs.shipa.discord.model.Attachment
import dev.capybaralabs.shipa.discord.model.Permission
import dev.capybaralabs.shipa.discord.model.Role
import dev.capybaralabs.shipa.discord.model.StringBitfield
import dev.capybaralabs.shipa.discord.model.User
import java.time.Instant
import java.util.Optional

/**
 * [Discord Resolved Data Structure](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-resolved-data-structure)
 */
data class ResolvedData(
	val users: Map<Long, User>?,
	val members: Map<Long, PartialMember>?,
	val roles: Map<Long, Role>?,
	val channels: Map<Long, PartialChannel>?,
	val messages: Map<Long, PartialMessage>?,
	val attachments: Map<Long, Attachment>?,
)


/**
 * [Discord Member](https://discord.com/developers/docs/resources/guild#guild-member-object) with missing fields as
 * according to [Resolved Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-resolved-data-structure)
 */
data class PartialMember(
	val nick: Optional<String>?,
	val avatar: Optional<String>?,
	val roles: List<Long>,
	val joinedAt: Instant,
	val premiumSince: Optional<Instant>?,
	val pending: Boolean?,
	val permissions: StringBitfield<Permission>?,
	val communicationDisabledUntil: Optional<Instant>?,
)


/**
 * [Discord Channel](https://discord.com/developers/docs/resources/channel#channel-object) with included fields as
 * described in [Resolved Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-resolved-data-structure)
 */
data class PartialChannel(
	val id: Long,
	val type: Int,
	val name: Optional<String>?,
	val permissions: StringBitfield<Permission>?,
//	val threadMetadata: ThreadMetadata?,
	val parentId: Optional<Long>?,
)

/**
 * [Discord Message](https://discord.com/developers/docs/resources/channel#message-object) with unknown missing fields as
 * according to [Resolved Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-resolved-data-structure)
 */
data class PartialMessage(
	val content: String,
)

