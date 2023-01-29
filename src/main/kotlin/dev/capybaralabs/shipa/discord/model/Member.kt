package dev.capybaralabs.shipa.discord.model

import java.time.Instant
import java.util.Optional
import kotlin.jvm.optionals.getOrNull


/**
 * [Discord Member](https://discord.com/developers/docs/resources/guild#guild-member-object)
 */
open class Member(
	open val user: User,  // only nullable in gateway events
	val nick: Optional<String>?,
	val avatar: Optional<String>?,
	val roles: List<Long>,
	val joinedAt: Instant,
	val premiumSince: Optional<Instant>?,
	val deaf: Boolean,
	val mute: Boolean,
	val pending: Boolean?,
	open val permissions: StringBitfield<Permission>?,
	val communicationDisabledUntil: Optional<Instant>?,
)

class InteractionMember(
	user: User,
	nick: Optional<String>?,
	avatar: Optional<String>?,
	roles: List<Long>,
	joinedAt: Instant,
	premiumSince: Optional<Instant>?,
	deaf: Boolean,
	mute: Boolean,
	pending: Boolean?,
	override val permissions: StringBitfield<Permission>,
	communicationDisabledUntil: Optional<Instant>?,
) : Member(user, nick, avatar, roles, joinedAt, premiumSince, deaf, mute, pending, permissions, communicationDisabledUntil) {

	fun avatarUrl(guildId: Long): String {
		return avatar?.getOrNull()?.let { avatarHash ->
			val ext = if (avatarHash.startsWith("a_")) "gif" else "png"
			"https://cdn.discordapp.com/guilds/$guildId/users/${user.id}/avatars/$avatarHash.$ext"
		} ?: user.avatarUrl()
	}
}
