package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import org.springframework.stereotype.Service

@Service
class DiscordEntityRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) {

	val channel: DiscordChannelRestService = DiscordChannelRestService(properties, discordRestService)
	val guild: DiscordGuildRestService = DiscordGuildRestService(properties, discordRestService)
	val user: DiscordUserRestService = DiscordUserRestService(properties, discordRestService)

}
