package dev.capybaralabs.shipa.discord.model

import java.util.Optional

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
	val flags: Int?,
	val premiumType: Int?,
	val publicFlags: Int?,
)
