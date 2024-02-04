package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordAuthToken
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import org.springframework.stereotype.Service

@Service
class DiscordEntityRestService(
	private val properties: DiscordProperties,
	private val discordRestService: DiscordRestService,
) {

	val application = DiscordApplicationRestService(properties, discordRestService)
	val channel = DiscordChannelRestService(properties, discordRestService)
	val emoji = DiscordEmojiRestService(properties, discordRestService)
	val guild = DiscordGuildRestService(properties, discordRestService)
	val invite = DiscordInviteRestService(properties, discordRestService)
	val user = DiscordUserRestService(properties, discordRestService)


	fun onBehalfOf(oauth2UserAccessToken: DiscordAuthToken.Oauth2): DiscordEntityRestService {
		return DiscordEntityRestService(properties, discordRestService.withUser(oauth2UserAccessToken))
	}

}
