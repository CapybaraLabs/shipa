package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Service
class CommandRegisterService(
	private val properties: DiscordProperties,
	private val restTemplate: RestTemplate,
) {

	fun register(command: CreateCommand, guildId: Long? = null) {
		if (guildId != null) {
			registerInGuild(command, guildId)
		} else {
			registerGlobally(command)
		}
	}

	fun registerGlobally(command: CreateCommand) {
		// TODO how to handle / log errors?
		restTemplate.postForEntity<Void>(
			"https://discord.com/api/v10/applications/{applicationId}/commands",
			command,
			properties.applicationId,
		)
	}


	fun registerInGuild(command: CreateCommand, guildId: Long) {
		// TODO how to handle / log errors?
		restTemplate.postForEntity<Void>(
			"https://discord.com/api/v10/applications/{applicationId}/guilds/{guildId}/commands",
			command,
			properties.applicationId,
			guildId,
		)
	}

}
