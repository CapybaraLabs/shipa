package dev.capybaralabs.shipa.discord.interaction.model

import dev.capybaralabs.shipa.discord.client.FileUpload
import dev.capybaralabs.shipa.discord.client.PayloadWithFiles
import dev.capybaralabs.shipa.discord.client.WithAttachments
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallback.Flags
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.CHANNEL_MESSAGE_WITH_SOURCE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.DEFERRED_UPDATE_MESSAGE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.MODAL
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.PONG
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.UPDATE_MESSAGE
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ActionRow
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ModalActionRow
import dev.capybaralabs.shipa.discord.model.AllowedMentions
import dev.capybaralabs.shipa.discord.model.Embed
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.MessageFlag
import dev.capybaralabs.shipa.discord.model.PartialAttachment
import dev.capybaralabs.shipa.discord.model.ZERO_WIDTH_SPACE

/**
 * [Discord Interaction Response](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-interaction-response-structure)
 */
sealed interface InteractionResponse {
	val type: InteractionCallbackType
	val data: InteractionCallback?

	data object Pong : InteractionResponse {
		override val type = PONG
		override val data: Nothing? = null
	}

	data class SendMessage(
		override val data: InteractionCallback.Message,
	) : InteractionResponse {

		override val type = CHANNEL_MESSAGE_WITH_SOURCE
	}

	data class Ack(
		override val data: Flags? = null,
	) : InteractionResponse {
		override val type = DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE
	}

	//Only valid for message-component interactions
	data object AckUpdate : InteractionResponse {
		override val type = DEFERRED_UPDATE_MESSAGE
		override val data: Nothing? = null
	}

	//Only valid for message-component interactions
	data class UpdateMessage(
		override val data: InteractionCallback.Message,
	) : InteractionResponse {
		override val type = UPDATE_MESSAGE
	}

	data class Autocomplete(
		override val data: InteractionCallback.Autocomplete,
	) : InteractionResponse {

		override val type = APPLICATION_COMMAND_AUTOCOMPLETE_RESULT
	}

	data class Modal(
		override val data: InteractionCallback.Modal,
	) : InteractionResponse {

		override val type = MODAL
	}
}


/**
 * [Discord Interaction Callback Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-interaction-callback-data-structure)
 */
sealed interface InteractionCallback {

	data class Flags(
		val flags: IntBitfield<MessageFlag>? = null, // EPHEMERAL only
	) : InteractionCallback

	/**
	 * [Discord Interaction Message Callback](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-messages)
	 */
	data class Message(
		val content: String? = ZERO_WIDTH_SPACE, // set to null when updating to keep existing
		val components: List<ActionRow>? = listOf(), // set to null when updating to keep existing. max 5
		val embeds: List<Embed>? = listOf(), // set to null when updating to keep existing
		val flags: IntBitfield<MessageFlag>? = null, // SUPPRESS_EMBEDS & EPHEMERAL only
		val allowedMentions: AllowedMentions? = AllowedMentions.none(),
		val tts: Boolean? = null,
		override val attachments: List<PartialAttachment>? = null,
	) : InteractionCallback, WithAttachments<Message> {

		override fun copyWithAttachments(attachments: List<PartialAttachment>): Message {
			return copy(attachments = attachments)
		}
	}

	/**
	 * [Discord Interaction Followup Message](https://discord.com/developers/docs/interactions/receiving-and-responding#create-followup-message)
	 *
	 * These can NOT be used as immediate responses, only when sending our own request. They support sending files.
	 */
	data class FollowupMessage(
		val message: Message,
		override val files: List<FileUpload>? = null,
	) : PayloadWithFiles<Message> {
		override val payload = message
	}

	/**
	 * [Discord Interaction Response Callback Data Autocomplete](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-autocomplete)
	 */
	data class Autocomplete(
		val choices: List<OptionChoice>,
	) : InteractionCallback

	/**
	 * [Discord Interaction Response Callback Data Modal](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-modal)
	 */
	data class Modal(
		val customId: String,
		val title: String,
		val components: List<ModalActionRow>,
	) : InteractionCallback

}
