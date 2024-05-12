package dev.capybaralabs.shipa.discord.client

import dev.capybaralabs.shipa.discord.oauth2.OAuth2Scope

sealed interface DiscordAuthToken {

	val token: String

	fun authHeader(): String

	data class Bot(
		override val token: String,
	) : DiscordAuthToken {
		override fun authHeader(): String {
			return "Bot $token"
		}
	}

	data class Oauth2(
		override val token: String,
		val scopes: List<OAuth2Scope>,
	) : DiscordAuthToken {

		override fun authHeader(): String {
			return "Bearer $token"
		}
	}
}
