package dev.capybaralabs.shipa.discord.model

import java.util.Optional

/**
 * [Discord Guild Preview Object](https://discord.com/developers/docs/resources/guild#guild-preview-object)
 */
data class GuildPreview(
	val id: Long,
	val name: String,
	val icon: Optional<String>,
	val splash: Optional<String>,
	val discoverySplash: Optional<String>,
//	val emojis: List<Emoji>,
	val features: List<GuildFeature>,
	val approximateMemberCount: Int,
	val approximatePresenceCount: Int,
	val description: Optional<String>,
//	val stickers: List<Sticker>,
)
