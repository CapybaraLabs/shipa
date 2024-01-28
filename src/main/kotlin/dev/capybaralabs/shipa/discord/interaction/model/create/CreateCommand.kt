package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandOption
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.CHAT_INPUT
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.MESSAGE
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.USER
import dev.capybaralabs.shipa.discord.model.Permission
import dev.capybaralabs.shipa.discord.model.StringBitfield
import java.util.Optional

/**
 * [Discord Application Command](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-structure)
 * [Discord Making a Global Command](https://discord.com/developers/docs/interactions/application-commands#making-a-global-command)
 * [Discord Making a Guild Command](https://discord.com/developers/docs/interactions/application-commands#making-a-guild-command)
 */
sealed interface CreateCommand {
	val type: ApplicationCommandType
	val name: String
	val defaultMemberPermissions: Optional<StringBitfield<Permission>>
		get() = Optional.empty()

	interface GlobalCommand : CreateCommand {
		val dmPermission: Boolean?
	}

	interface GuildCommand : CreateCommand {
		val guildIds: List<Long>
	}

	data class CreateUserGlobalCommand(
		override val name: String,
		override val dmPermission: Boolean? = null,
		override val defaultMemberPermissions: Optional<StringBitfield<Permission>> = Optional.empty(),
	) : GlobalCommand {
		override val type = USER
	}

	data class CreateUserGuildCommand(
		override val name: String,
		override val guildIds: List<Long>,
		override val defaultMemberPermissions: Optional<StringBitfield<Permission>> = Optional.empty(),
	) : GuildCommand {
		override val type = USER
	}


	data class CreateMessageGlobalCommand(
		override val name: String,
		override val dmPermission: Boolean? = null,
		override val defaultMemberPermissions: Optional<StringBitfield<Permission>> = Optional.empty(),
	) : GlobalCommand {
		override val type = MESSAGE
	}

	data class CreateMessageGuildCommand(
		override val name: String,
		override val guildIds: List<Long>,
		override val defaultMemberPermissions: Optional<StringBitfield<Permission>> = Optional.empty(),
	) : GuildCommand {
		override val type = MESSAGE
	}


	interface SlashCommand {
		val description: String
		val options: List<ApplicationCommandOption>?
			get() = null
	}

	data class CreateSlashGlobalCommand(
		override val name: String,
		override val description: String,
		override val dmPermission: Boolean? = null,
		override val defaultMemberPermissions: Optional<StringBitfield<Permission>> = Optional.empty(),
		override val options: List<ApplicationCommandOption>? = null,
	) : GlobalCommand, SlashCommand {
		override val type = CHAT_INPUT
	}

	data class CreateSlashGuildCommand(
		override val name: String,
		override val description: String,
		override val guildIds: List<Long>,
		override val defaultMemberPermissions: Optional<StringBitfield<Permission>> = Optional.empty(),
		override val options: List<ApplicationCommandOption>? = null,
	) : GuildCommand, SlashCommand {
		override val type = CHAT_INPUT
	}

}
