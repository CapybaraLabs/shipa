package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType

/**
 * [Discord Application Command](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-structure)
 * [Discord Making a Global Command](https://discord.com/developers/docs/interactions/application-commands#making-a-global-command)
 * [Discord Making a Guild Command](https://discord.com/developers/docs/interactions/application-commands#making-a-guild-command)
 */
abstract class CreateCommand(
	val type: ApplicationCommandType
)
