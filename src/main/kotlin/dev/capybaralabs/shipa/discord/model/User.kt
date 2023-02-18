package dev.capybaralabs.shipa.discord.model

import dev.capybaralabs.shipa.discord.model.ImageFormatting.Format.GIF
import dev.capybaralabs.shipa.discord.model.ImageFormatting.Format.PNG
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/**
 * [Discord User](https://discord.com/developers/docs/resources/user#user-object)
 */
data class User(
	val id: Long,
	val username: String,
	val discriminator: String,
	val avatar: Optional<String>,
	val bot: Boolean?,
	val system: Boolean?,
	val mfaEnabled: Boolean?,
	val banner: Optional<String>?,
	val accentColor: Optional<Int>?,
	val locale: String?,
	val verified: Boolean?,
	val email: Optional<String>?,
	val flags: IntBitfield<UserFlag>?,
	val premiumType: Int?,
	val publicFlags: IntBitfield<UserFlag>?,
) {

	fun asMention(): String {
		return "<@$id>"
	}

	fun effectiveAvatarUrl(): String {
		return avatarUrl() ?: defaultAvatarUrl()
	}

	private fun defaultAvatarUrl(): String {
		return ImageFormatting.imageUrl("/embed/avatars/${discriminator.toInt() % 5}", PNG)
	}

	fun avatarUrl(): String? {
		return avatar.getOrNull()?.let {
			val format = if (it.startsWith("a_")) GIF else PNG
			ImageFormatting.imageUrl("/avatars/$id/$it", format)
		}
	}

	fun bannerUrl(): String? {
		return banner?.getOrNull()?.let {
			val format = if (it.startsWith("a_")) GIF else PNG
			ImageFormatting.imageUrl("/banners/$id/$it", format)
		}
	}

	fun tag(): String {
		return "$username#$discriminator"
	}
}

enum class UserFlag(override val value: Int) : IntBitflag {
	STAFF(1 shl 0),
	PARTNER(1 shl 1),
	HYPESQUAD(1 shl 2),
	BUG_HUNTER_LEVEL_1(1 shl 3),
	HYPESQUAD_ONLINE_HOUSE_1(1 shl 6),
	HYPESQUAD_ONLINE_HOUSE_2(1 shl 7),
	HYPESQUAD_ONLINE_HOUSE_3(1 shl 8),
	PREMIUM_EARLY_SUPPORTER(1 shl 9),
	TEAM_PSEUDO_USER(1 shl 10),
	BUG_HUNTER_LEVEL_2(1 shl 14),
	VERIFIED_BOT(1 shl 16),
	VERIFIED_DEVELOPER(1 shl 17),
	CERTIFIED_MODERATOR(1 shl 18),
	BOT_HTTP_INTERACTIONS(1 shl 19),
}
