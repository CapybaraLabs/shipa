package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Service
class CommandRegisterService(
	private val properties: DiscordProperties,
	private val restTemplate: RestTemplate,
	private val environment: Environment,
) {

	private fun isTestEnvironment(): Boolean {
		return environment.acceptsProfiles(Profiles.of("test"))
	}

	fun register(command: CreateCommand, guildId: Long? = null) {
		if (guildId != null) {
			registerInGuild(command, guildId)
		} else {
			registerGlobally(command)
		}
	}

	fun registerGlobally(command: CreateCommand) {
		if (isTestEnvironment()) return

		restTemplate.postForEntity<Void>(
			"/applications/{applicationId}/commands",
			command,
			properties.applicationId,
		)
	}


	fun registerInGuild(command: CreateCommand, guildId: Long) {
		if (isTestEnvironment()) return

		restTemplate.postForEntity<Void>(
			"/applications/{applicationId}/guilds/{guildId}/commands",
			command,
			properties.applicationId,
			guildId,
		)
	}

	fun deleteCommand(commandId: Long, guildId: Long?) {
		if (guildId != null) {
			deleteGuildCommand(commandId, guildId)
		} else {
			deleteGlobalCommand(commandId)
		}
	}

	fun deleteGlobalCommand(commandId: Long) {
		if (isTestEnvironment()) return

		restTemplate.delete("/applications/{applicationId}/commands/{commandId}", properties.applicationId, commandId)
	}

	fun deleteGuildCommand(commandId: Long, guildId: Long) {
		if (isTestEnvironment()) return

		restTemplate.delete("/applications/{applicationId}/guilds/{guildId}/commands/{commandId}", properties.applicationId, guildId, commandId)
	}
}
