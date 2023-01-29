package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.model.Application
import org.springframework.http.RequestEntity


/**
 * Rest Client for the [Discord Application Resource](https://discord.com/developers/docs/resources/application)
 */
class DiscordApplicationRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

	// https://discord.com/developers/docs/topics/oauth2#get-current-bot-application-information
	suspend fun fetchCurrentBotApplicationInfo(): Application {
		return discordRestService.exchange<Application>(
			"$applicationId",
			RequestEntity
				.get("/oauth2/applications/@me")
				.build()
		).body!!
	}
}
