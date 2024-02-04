package dev.capybaralabs.shipa.discord.client

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
	) : DiscordAuthToken {
		override fun authHeader(): String {
			return "Bearer $token"
		}
	}
}
