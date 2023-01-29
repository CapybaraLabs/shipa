package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import org.springframework.stereotype.Service

@Service
class DiscordEntityRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) {

	val application = DiscordApplicationRestService(properties, discordRestService)
	val channel = DiscordChannelRestService(properties, discordRestService)
	val emoji = DiscordEmojiRestService(properties, discordRestService)
	val guild = DiscordGuildRestService(properties, discordRestService)
	val invite = DiscordInviteRestService(properties, discordRestService)
	val user = DiscordUserRestService(properties, discordRestService)

}
