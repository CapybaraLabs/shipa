package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.client.ratelimit.Gateway
import org.springframework.http.RequestEntity

/**
 * Rest Client for the [Discord Gateway](https://discord.com/developers/docs/topics/gateway#get-gateway)
 */

class DiscordGatewayRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

	/**
	 * [Get Gateway](https://discord.com/developers/docs/topics/gateway#get-gateway)
	 */
	suspend fun getGateway(): Gateway {
		return discordRestService.exchange<Gateway>(
			Gateway,
			RequestEntity
				.get("/gateway")
				.build(),
		).body!!
	}

	data class Gateway(
		val url: String,
	)

	/**
	 * [Get Gateway Bot](https://discord.com/developers/docs/topics/gateway#get-gateway-bot)
	 */
	suspend fun getGatewayBot(): GatewayBot {
		return discordRestService.exchange<GatewayBot>(
			Gateway,
			RequestEntity
				.get("/gateway/bot")
				.build(),
		).body!!
	}

	data class GatewayBot(
		val url: String,
		val shards: Int,
		val sessionStartLimit: SessionStartLimit,
	)

	/**
	 * [Discord Gateway Bot Session Start Limit](https://discord.com/developers/docs/topics/gateway#session-start-limit-object)
	 */
	data class SessionStartLimit(
		val total: Int,
		val remaining: Int,
		val resetAfter: Int,
		val maxConcurrency: Int,
	)

}

