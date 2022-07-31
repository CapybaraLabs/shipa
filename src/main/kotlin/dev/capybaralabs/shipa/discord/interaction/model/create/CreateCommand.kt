package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandOption
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.CHAT_INPUT
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.USER
import java.util.Optional

/**
 * [Discord Application Command](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-structure)
 * [Discord Making a Global Command](https://discord.com/developers/docs/interactions/application-commands#making-a-global-command)
 * [Discord Making a Guild Command](https://discord.com/developers/docs/interactions/application-commands#making-a-guild-command)
 */
abstract class CreateCommand(
	val type: ApplicationCommandType,
	open val name: String,
	open val guildId: Long? = null
) {

	data class User(
		override val name: String,
		override val guildId: Long? = null,
	) : CreateCommand(USER, name, guildId)


	data class Slash(
		override val name: String,
		val description: String,
		override val guildId: Long? = null,
		val options: List<ApplicationCommandOption>? = null,
		val defaultMemberPermissions: Optional<String> = Optional.empty(), // Admin only: Optional.of("0")
		val dmPermission: Boolean? = null,
	) : CreateCommand(CHAT_INPUT, name, guildId)
}
