package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.model.Channel
import org.springframework.http.RequestEntity

/**
 * Rest Client for the [Discord Channels Resource](https://discord.com/developers/docs/resources/channel)
 */
class DiscordChannelRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

	// https://discord.com/developers/docs/resources/channel#get-channel
	suspend fun fetchChannel(channelId: Long): Channel {
		return discordRestService.exchange<Channel>(
			"$applicationId-$channelId",
			RequestEntity
				.get("/channels/{channelId}", channelId)
				.build()
		).body!!
	}

}
