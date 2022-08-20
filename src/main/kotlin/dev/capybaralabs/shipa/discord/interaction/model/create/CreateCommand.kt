package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandOption
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.CHAT_INPUT
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.USER
import dev.capybaralabs.shipa.discord.model.Permission
import dev.capybaralabs.shipa.discord.model.StringBitfield
import java.util.Optional

/**
 * [Discord Application Command](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-structure)
 * [Discord Making a Global Command](https://discord.com/developers/docs/interactions/application-commands#making-a-global-command)
 * [Discord Making a Guild Command](https://discord.com/developers/docs/interactions/application-commands#making-a-guild-command)
 */
abstract class CreateCommand(
	val type: ApplicationCommandType,
	open val name: String,
	open val guildIds: List<Long>? = null
) {

	data class User(
		override val name: String,
		override val guildIds: List<Long>? = null,
	) : CreateCommand(USER, name, guildIds)


	data class Slash(
		override val name: String,
		val description: String,
		override val guildIds: List<Long>? = null,
		val options: List<ApplicationCommandOption>? = null,
		val defaultMemberPermissions: Optional<StringBitfield<Permission>> = Optional.empty(),
		val dmPermission: Boolean? = null,
	) : CreateCommand(CHAT_INPUT, name, guildIds)
}
