package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.ShipaDiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.client.ratelimit.ApplicationsCommands
import dev.capybaralabs.shipa.discord.client.ratelimit.ApplicationsCommandsId
import dev.capybaralabs.shipa.discord.client.ratelimit.ApplicationsGuildsCommands
import dev.capybaralabs.shipa.discord.client.ratelimit.ApplicationsGuildsCommandsId
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommand
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand.GlobalCommand
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand.GuildCommand
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service

@Service
class CommandRegisterService(
	properties: ShipaDiscordProperties,
	private val discordRestService: DiscordRestService,
	private val environment: Environment,
) {

	private val applicationId = properties.applicationId

	private fun isTestEnvironment(): Boolean {
		return environment.acceptsProfiles(Profiles.of("test"))
	}

	suspend fun bulkOverwrite(commands: List<CreateCommand>): List<ApplicationCommand> {
		val globalCommands = ArrayList<GlobalCommand>()
		val guildCommands = ArrayList<GuildCommand>()
		commands.forEach {
			when (it) {
				is GuildCommand -> guildCommands += it
				is GlobalCommand -> globalCommands += it
			}
		}

		val globalApplicationCommands = bulkOverwriteGlobally(globalCommands)

		val guildApplicationCommands = guildCommands.map { it.guildIds to it }
			.flatMap { it.first.map { guildId -> guildId to it.second } }
			.groupBy({ it.first }) { it.second }
			.flatMap { (guildId, commands) -> bulkOverwriteGuild(guildId, commands) }

		return globalApplicationCommands + guildApplicationCommands
	}

	suspend fun bulkOverwriteGlobally(commands: List<GlobalCommand>): List<ApplicationCommand> {
		if (isTestEnvironment() || commands.isEmpty()) return listOf()

		return discordRestService.exchange<List<ApplicationCommand>>(
			ApplicationsCommands(applicationId),
			RequestEntity
				.put("/applications/{applicationId}/commands", applicationId)
				.body(commands),
		).body!!
	}

	suspend fun bulkOverwriteGuild(guildId: Long, commands: List<GuildCommand>): List<ApplicationCommand> {
		if (isTestEnvironment() || commands.isEmpty()) return listOf()

		return discordRestService.exchange<List<ApplicationCommand>>(
			ApplicationsGuildsCommands(applicationId),
			RequestEntity
				.put("/applications/{applicationId}/guilds/{guildId}/commands", applicationId, guildId)
				.body(commands),
		).body!!
	}

	suspend fun register(command: CreateCommand) {
		when (command) {
			is GuildCommand -> for (guildId in command.guildIds) {
				registerInGuild(command, guildId)
			}

			is GlobalCommand -> registerGlobally(command)
		}
	}

	suspend fun registerGlobally(command: GlobalCommand) {
		if (isTestEnvironment()) return

		discordRestService.exchange<Void>(
			ApplicationsCommands(applicationId),
			RequestEntity
				.post("/applications/{applicationId}/commands", applicationId)
				.body(command),
		)
	}


	suspend fun registerInGuild(command: GuildCommand, guildId: Long) {
		if (isTestEnvironment()) return

		discordRestService.exchange<Void>(
			ApplicationsGuildsCommands(applicationId),
			RequestEntity
				.post("/applications/{applicationId}/guilds/{guildId}/commands", applicationId, guildId)
				.body(command),
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

		discordRestService.exchange<Void>(
			ApplicationsCommandsId(applicationId),
			RequestEntity
				.delete("/applications/{applicationId}/commands/{commandId}", applicationId, commandId)
				.build(),
		)
	}

	suspend fun deleteGuildCommand(commandId: Long, guildId: Long) {
		if (isTestEnvironment()) return

		discordRestService.exchange<Void>(
			ApplicationsGuildsCommandsId(applicationId),
			RequestEntity
				.delete("/applications/{applicationId}/guilds/{guildId}/commands/{commandId}", applicationId, guildId, commandId)
				.build(),
		)
	}
}
