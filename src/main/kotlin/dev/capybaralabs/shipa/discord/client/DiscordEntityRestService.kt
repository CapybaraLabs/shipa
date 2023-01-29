package dev.capybaralabs.shipa.discord.client

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.model.Channel
import dev.capybaralabs.shipa.discord.model.Guild
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service

@Service
class DiscordEntityRestService(
	properties: DiscordProperties,
	private val restService: RestService,
) {
	private val applicationId = properties.applicationId


	// https://discord.com/developers/docs/resources/channel#get-channel
	suspend fun fetchChannel(channelId: Long): Channel {
		return restService.exchange<Channel>(
			"$applicationId-$channelId",
			RequestEntity
				.get("/channels/{channelId}", channelId)
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/guild#get-guild
	suspend fun fetchGuild(guildId: Long): Guild {
		return restService.exchange<Guild>(
			"$applicationId-$guildId",
			RequestEntity
				.get("/guilds/{guildId}", guildId)
				.build()
		).body!!
	}
}
