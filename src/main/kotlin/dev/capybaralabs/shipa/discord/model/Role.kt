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
	val permissions: StringBitfield<Permission>,
	val managed: Boolean,
	val mentionable: Boolean,
	val tags: RoleTags?,
)

/**
 * [Discord Role Tags](https://discord.com/developers/docs/topics/permissions#role-object-role-tags-structure)
 */
data class RoleTags(
	val botId: Long?,
	val integrationId: Long?,
	val premiumSubscriber: Void?,
)
