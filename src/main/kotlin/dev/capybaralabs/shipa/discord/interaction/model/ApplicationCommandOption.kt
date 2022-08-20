package dev.capybaralabs.shipa.discord.interaction.model

import dev.capybaralabs.shipa.discord.model.ChannelType

/**
 * [Discord Application Command Option](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-option-structure)
 */
data class ApplicationCommandOption(
	val type: ApplicationCommandOptionType,
	val name: String,
	val description: String,
	val required: Boolean?,
	val choices: List<ApplicationCommandOptionChoice>? = null,
	val options: List<ApplicationCommandOption>? = null,
	val channelTypes: List<ChannelType>? = null,
	val minValue: Int? = null, // or Double
	val maxValue: Int? = null, // or Double
	val minLength: Int? = null,
	val maxLength: Int? = null,
	val autocomplete: Boolean? = null,
)


data class ApplicationCommandOptionChoice(
	val name: String,
	val value: String, // or Int or Double
)
