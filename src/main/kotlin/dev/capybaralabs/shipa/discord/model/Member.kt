package dev.capybaralabs.shipa.discord.model

import java.time.Instant
import java.util.Optional


/**
 * [Discord Member](https://discord.com/developers/docs/resources/guild#guild-member-object)
 */
open class Member(
	open val user: User?,
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
	override val user: User, // only nullable in gateway events
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
) : Member(user, nick, avatar, roles, joinedAt, premiumSince, deaf, mute, pending, permissions, communicationDisabledUntil)
