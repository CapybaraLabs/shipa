package dev.capybaralabs.shipa.discord.model

import java.time.Instant
import java.util.Optional


/**
 * [Discord Member](https://discord.com/developers/docs/resources/guild#guild-member-object)
 */
data class Member(
	val user: User, // only nullable in gateway events
	val nick: Optional<String>?,
	val avatar: Optional<String>?,
	val roles: List<Long>,
	val joinedAt: Instant,
	val premiumSince: Optional<Instant>?,
	val deaf: Boolean,
	val mute: Boolean,
	val pending: Boolean?,
	val permissions: String?,
	val communicationDisabledUntil: Optional<Instant>?,
)
