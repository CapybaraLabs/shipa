package dev.capybaralabs.shipa.discord.model

import java.util.Optional

/**
 * [Discord Role](https://discord.com/developers/docs/topics/permissions#role-object)
 */
data class Role(
	val id: Long,
	val name: String,
	val color: Int,
	val hoist: Boolean,
	val icon: Optional<String>?,
	val unicodeEmoji: Optional<String>?,
	val position: Int,
	val permissions: String,
	val managed: Boolean,
	val mentionable: Boolean,
	val tags: List<RoleTag>?,
)

/**
 * [Discord Role Tag](https://discord.com/developers/docs/topics/permissions#role-object-role-tags-structure)
 */
data class RoleTag(
	val botId: Long?,
	val integrationId: Long?,
	val premiumSubscriber: Void?,
)
