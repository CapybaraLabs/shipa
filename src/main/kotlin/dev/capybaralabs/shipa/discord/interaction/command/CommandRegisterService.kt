package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.RestService
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommand
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service

@Service
class CommandRegisterService(
	properties: DiscordProperties,
	private val restService: RestService,
	private val environment: Environment,
) {

	private val applicationId = properties.applicationId

	private fun isTestEnvironment(): Boolean {
		return environment.acceptsProfiles(Profiles.of("test"))
	}

	suspend fun bulkOverwrite(commands: List<CreateCommand>): List<ApplicationCommand> {
		val groupBy: Map<Boolean, List<CreateCommand>> = commands.groupBy { it.guildIds == null }
		val globalCommands = groupBy[true] ?: listOf()
		val guildCommands = groupBy[false] ?: listOf()

		val globalApplicationCommands = bulkOverwriteGlobally(globalCommands)

		val guildApplicationCommands = guildCommands.map { it.guildIds!! to it }
			.flatMap { it.first.map { guildId -> guildId to it.second } }
			.groupBy({ it.first }) { it.second }
			.flatMap { (guildId, commands) -> bulkOverwriteGuild(guildId, commands) }

		return globalApplicationCommands + guildApplicationCommands
	}

	suspend fun bulkOverwriteGlobally(commands: List<CreateCommand>): List<ApplicationCommand> {
		if (isTestEnvironment() || commands.isEmpty()) return listOf()

		return restService.exchange<List<ApplicationCommand>>(
			"$applicationId",
			RequestEntity
				.put("/applications/{applicationId}/commands", applicationId)
				.body(commands)
		).body!!
	}

	suspend fun bulkOverwriteGuild(guildId: Long, commands: List<CreateCommand>): List<ApplicationCommand> {
		if (isTestEnvironment() || commands.isEmpty()) return listOf()

		return restService.exchange<List<ApplicationCommand>>(
			"$applicationId-$guildId",
			RequestEntity
				.put("/applications/{applicationId}/guilds/{guildId}/commands", applicationId, guildId)
				.body(commands)
		).body!!
	}

	suspend fun register(command: CreateCommand) {
		val guildIds = command.guildIds
		if (guildIds != null) {
			for (guildId in guildIds) {
				registerInGuild(command, guildId)
			}
		} else {
			registerGlobally(command)
		}
	}

	suspend fun registerGlobally(command: CreateCommand) {
		if (isTestEnvironment()) return

		restService.exchange<Void>(
			"$applicationId",
			RequestEntity
				.post("/applications/{applicationId}/commands", applicationId)
				.body(command)
		)
	}


	suspend fun registerInGuild(command: CreateCommand, guildId: Long) {
		if (isTestEnvironment()) return

		restService.exchange<Void>(
			"$applicationId-$guildId",
			RequestEntity
				.post("/applications/{applicationId}/guilds/{guildId}/commands", applicationId, guildId)
				.body(command)
		)
	}

	suspend fun deleteCommand(commandId: Long, guildId: Long?) {
		if (guildId != null) {
			deleteGuildCommand(commandId, guildId)
		} else {
			deleteGlobalCommand(commandId)
		}
	}

	suspend fun deleteGlobalCommand(commandId: Long) {
		if (isTestEnvironment()) return

		restService.exchange<Void>(
			"$applicationId",
			RequestEntity
				.delete("/applications/{applicationId}/commands/{commandId}", applicationId, commandId)
				.build()
		)
	}

	suspend fun deleteGuildCommand(commandId: Long, guildId: Long) {
		if (isTestEnvironment()) return

		restService.exchange<Void>(
			"$applicationId-$guildId",
			RequestEntity
				.delete("/applications/{applicationId}/guilds/{guildId}/commands/{commandId}", applicationId, guildId, commandId)
				.build()
		)
	}
}
