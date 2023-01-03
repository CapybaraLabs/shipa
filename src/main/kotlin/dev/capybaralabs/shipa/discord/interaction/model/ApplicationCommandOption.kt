package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dev.capybaralabs.shipa.discord.model.ChannelType

/**
 * [Discord Application Command Option](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-option-structure)
 */
data class ApplicationCommandOption(
	val type: ApplicationCommandOptionType,
	val name: String,
	val description: String,
	val required: Boolean? = null, // defaults to false on Discord's end
	val choices: List<ApplicationCommandOptionChoice>? = null,
	val options: List<ApplicationCommandOption>? = null,
	val channelTypes: List<ChannelType>? = null,
	val minValue: Int? = null, // or Double
	val maxValue: Int? = null, // or Double
	val minLength: Int? = null,
	val maxLength: Int? = null,
	val autocomplete: Boolean? = null,
)

interface ApplicationCommandOptionChoice {
	val name: String
	val value: Any

	companion object {
		@JsonCreator
		@JvmStatic
		fun create(
			@JsonProperty("name") name: String,
			@JsonProperty("value") value: Any,
		): ApplicationCommandOptionChoice {
			return when (value) {
				is String -> StringChoice(name, value)
				is Int -> IntChoice(name, value)
				is Double -> DoubleChoice(name, value)
				else -> throw IllegalArgumentException("Unhandled choice type ${value.javaClass}")
			}
		}
	}

	data class StringChoice(
		override val name: String,
		override val value: String,
	) : ApplicationCommandOptionChoice

	data class IntChoice(
		override val name: String,
		override val value: Int,
	) : ApplicationCommandOptionChoice

	data class DoubleChoice(
		override val name: String,
		override val value: Double,
	) : ApplicationCommandOptionChoice

}
