package dev.capybaralabs.shipa.discord.model

import java.time.Instant
import java.util.Optional

/**
 * [Discord Invite Object](https://discord.com/developers/docs/resources/invite#invite-object)
 */
data class Invite(
	val code: String,
//	val guild: PartialGuild?,
//	val channel: Optional<PartialChannel>,
	val inviter: User?,
	val targetType: Int?,
	val targetUser: User?,
//	val targetApplication: Application?,
	val approximatePresenceCount: Int?,
	val approximateMemberCount: Int?,
	val expiresAt: Optional<Instant>?,
//	val guildScheduledEvent: GuildScheduledEvent?,

	// metadata. optional, requires GUILD_MANAGE permissions or maybe ownership
	val uses: Int?,
	val maxUses: Int?,
	val maxAge: Int?,
	val temporary: Boolean?,
	val createdAt: Instant?, // seems to be missing for really old invites?
)
