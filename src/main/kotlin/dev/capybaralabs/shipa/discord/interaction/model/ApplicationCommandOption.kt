package dev.capybaralabs.shipa.discord.interaction.model

/**
 * [Discord Application Command Option](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-option-structure)
 */
data class ApplicationCommandOption(
	val type: ApplicationCommandOptionType,
	val name: String,
	val description: String,
	val required: Boolean?,
)
