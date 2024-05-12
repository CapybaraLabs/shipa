package dev.capybaralabs.shipa.discord.model

import java.util.Optional

/**
 * [Partial Discord Guild Object](https://discord.com/developers/docs/resources/user#get-current-user-guilds-example-partial-guild)
 */
data class PartialGuild(
	override val id: Long,
	val name: String,
	override val icon: Optional<String>,
	val owner: Boolean?,
	val permissions: StringBitfield<Permission>?,
	val features: List<String>, // don't use a enum, these change frequently and many are undocumented
	val approximateMemberCount: Int?,
	val approximatePresenceCount: Int?,
) : HasGuildIcon
