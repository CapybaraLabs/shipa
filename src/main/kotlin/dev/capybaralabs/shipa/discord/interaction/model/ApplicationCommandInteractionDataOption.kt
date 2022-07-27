package dev.capybaralabs.shipa.discord.interaction.model

/**
 * [Discord Application Command Interaction Data Option](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-interaction-data-option-structure)
 */
data class ApplicationCommandInteractionDataOption(
	val name: String,
	val type: ApplicationCommandOptionType,
	val value: String?, // or Int, or Double
	val options: List<ApplicationCommandInteractionDataOption>?,
	val focused: Boolean?,
)
