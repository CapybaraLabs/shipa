package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.DEDUCTION
import java.awt.TextComponent

/**
 * [Discord Interaction Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-interaction-data)
 */
@JsonTypeInfo(use = DEDUCTION)
sealed interface InteractionData {

	/**
	 * [Discord Application Command Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-application-command-data-structure)
	 */
	data class ApplicationCommandData(
		val id: Long,
		val name: String,
		val type: ApplicationCommandType,
		val resolved: ResolvedData?,
		val options: List<ApplicationCommandInteractionDataOption>?,
		val guildId: Long?,
		val targetId: Long?,
	) : InteractionData {

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
	}


	/**
	 * [Discord Message Component Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-message-component-data-structure)
	 */
	data class MessageComponentData(
		val customId: String,
		val componenType: MessageComponent.ComponentType,
		val values: List<MessageComponent.SelectMenu.SelectOption>?,
	) : InteractionData

	/**
	 * [Discord Modal Submit Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-modal-submit-data-structure)
	 */
	data class ModalSubmitData(
		val customId: String,
		val components: List<TextComponent>,
	) : InteractionData

}
