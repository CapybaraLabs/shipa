package dev.capybaralabs.shipa.discord.interaction.model

import dev.capybaralabs.shipa.discord.model.Permission
import dev.capybaralabs.shipa.discord.model.StringBitfield
import java.util.Optional

/**
 * [Discord Application Command](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-structure)
 */
data class ApplicationCommand(
	val id: Long,
	val type: ApplicationCommandType?,
	val applicationId: Long,
	val guildId: Long?,
	val name: String,
	val description: String,
	val options: List<ApplicationCommandOption>?,
	val defaultMemberPermissions: Optional<StringBitfield<Permission>>,
	val dmPermission: Boolean?,
	val version: Long,
)
