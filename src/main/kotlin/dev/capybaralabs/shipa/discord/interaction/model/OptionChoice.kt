package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

interface OptionChoice {
	val name: String
	val value: Any

	companion object {
		@JsonCreator
		@JvmStatic
		fun create(
			@JsonProperty("name") name: String,
			@JsonProperty("value") value: Any,
		): OptionChoice {
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
	) : OptionChoice

	data class IntChoice(
		override val name: String,
		override val value: Int,
	) : OptionChoice

	data class DoubleChoice(
		override val name: String,
		override val value: Double,
	) : OptionChoice

}
