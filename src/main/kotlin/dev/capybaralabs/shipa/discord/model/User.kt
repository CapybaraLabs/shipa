package dev.capybaralabs.shipa.discord.model

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

	@OptIn(ExperimentalStdlibApi::class)
	fun avatarUrl(): String {
		return avatar.getOrNull()?.let { avatarHash ->
			val ext = if (avatarHash.startsWith("a_")) "gif" else "png"
			"https://cdn.discordapp.com/avatars/${id}/$avatarHash.$ext"
		} ?: defaultAvatarUrl()
	}

	private fun defaultAvatarUrl(): String {
		return "https://cdn.discordapp.com/embed/avatars/${discriminator.toInt() % 5}.png"
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
