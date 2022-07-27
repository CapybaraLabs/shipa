package dev.capybaralabs.shipa.discord.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * [Discord Allowed Mentions](https://discord.com/developers/docs/resources/channel#allowed-mentions-object)
 */
data class AllowedMentions(
	val parse: List<AllowedMentionsType>,
	val roles: List<Long>,
	val users: List<Long>,
	val repliedUser: Boolean,
)

enum class AllowedMentionsType(@JsonValue val value: String) {
	ROLE_MENTIONS("roles"),
	USER_MENTIONS("users"),
	EVERYONE_MENTIONS("everyone"),
}
