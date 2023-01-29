package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService

abstract class BaseDiscordEntityRestService(
	properties: DiscordProperties,
	protected val discordRestService: DiscordRestService,
) {
	protected val applicationId = properties.applicationId

}
