package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandOption
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType
import java.util.Optional

/**
 * [Discord Application Command](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-structure)
 * [Discord Making a Global Command](https://discord.com/developers/docs/interactions/application-commands#making-a-global-command)
 * [Discord Making a Guild Command](https://discord.com/developers/docs/interactions/application-commands#making-a-guild-command)
 */
data class ApplicationCommandCreate(
	val type: ApplicationCommandType?,
	val name: String,
	val description: String,
	val options: List<ApplicationCommandOption>? = null,
	val defaultMemberPermissions: Optional<String> = Optional.empty(),
	val dmPermission: Boolean? = null,
)
