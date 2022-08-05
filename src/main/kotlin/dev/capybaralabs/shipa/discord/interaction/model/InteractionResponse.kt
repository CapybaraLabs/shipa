package dev.capybaralabs.shipa.discord.interaction.model

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.CHANNEL_MESSAGE_WITH_SOURCE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.DEFERRED_UPDATE_MESSAGE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.MODAL
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.PONG
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.UPDATE_MESSAGE
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.TextInput
import dev.capybaralabs.shipa.discord.model.AllowedMentions
import dev.capybaralabs.shipa.discord.model.Embed

/**
 * [Discord Interaction Response](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-interaction-response-structure)
 */
sealed interface InteractionResponse {
	val type: InteractionCallbackType
	val data: InteractionCallbackData?

	object Pong : InteractionResponse {
		override val type = PONG
		override val data: Nothing? = null
	}

	data class SendMessage(
		override val data: InteractionCallbackData.Message
	) : InteractionResponse {

		override val type = CHANNEL_MESSAGE_WITH_SOURCE
	}

	object Ack : InteractionResponse {
		override val type = DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE
		override val data: Nothing? = null
	}

	//Only valid for message-component interactions
	object AckUpdate : InteractionResponse {
		override val type = DEFERRED_UPDATE_MESSAGE
		override val data: Nothing? = null
	}

	//Only valid for message-component interactions
	data class UpdateMessage(
		override val data: InteractionCallbackData.Message
	) : InteractionResponse {
		override val type = UPDATE_MESSAGE
	}

	data class Autocomplete(
		override val data: InteractionCallbackData.Autocomplete
	) : InteractionResponse {

		override val type = APPLICATION_COMMAND_AUTOCOMPLETE_RESULT
	}

	data class Modal(
		override val data: InteractionCallbackData.Modal
	) : InteractionResponse {

		override val type = MODAL
	}
}


/**
 * [Discord Interaction Callback Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-interaction-callback-data-structure)
 */
sealed interface InteractionCallbackData {

	/**
	 * [Discord Interaction Message Callback](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-messages)
	 */
	data class Message(
		val tts: Boolean? = null,
		val content: String? = null,
		val embeds: List<Embed>? = listOf(), // set to null when updating to keep existing
		val allowedMentions: AllowedMentions? = null,
		val flags: Int? = null,
		val components: List<MessageComponent>? = listOf(), // set to null when updating to keep existing
//	val attachments: List<PartialAttachment>?,
	) : InteractionCallbackData

	/**
	 * [Discord Interaction Response Callback Data Autocomplete](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-autocomplete)
	 */
	data class Autocomplete(
		val choices: List<Choice>,
	) : InteractionCallbackData {

		data class Choice(
			val name: String,
			val value: String, // or Int or Double
		)
	}

	/**
	 * [Discord Interaction Response Callback Data Modal](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-modal)
	 */
	data class Modal(
		val customId: String,
		val title: String,
		val components: List<TextInput>,
	) : InteractionCallbackData

}
