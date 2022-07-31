package dev.capybaralabs.shipa.discord.interaction.model

import dev.capybaralabs.shipa.discord.interaction.model.InteractionData.ApplicationCommandData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionData.MessageComponentData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionData.ModalSubmitData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.Ping
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.APPLICATION_COMMAND
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.MESSAGE_COMPONENT
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.MODAL_SUBMIT
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.PING

/**
 * [Discord Interaction](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object)
 */
sealed interface InteractionObject {
	val id: Long
	val applicationId: Long
	val token: String
	val type: InteractionType
	val data: InteractionData?

	data class Ping(
		override val id: Long,
		override val applicationId: Long,
		override val token: String
	) : InteractionObject {
		override val data: Nothing? = null
		override val type = PING
	}

	sealed interface InteractionWithData : InteractionObject {
		override val data: InteractionData

		data class ApplicationCommand(
			override val id: Long,
			override val applicationId: Long,
			override val token: String,
			override val data: ApplicationCommandData,
		) : InteractionWithData {
			override val type = APPLICATION_COMMAND
		}

		data class MessageComponent(
			override val id: Long,
			override val applicationId: Long,
			override val token: String,
			override val data: MessageComponentData,
		) : InteractionWithData {
			override val type = MESSAGE_COMPONENT
		}

		data class Autocomplete(
			override val id: Long,
			override val applicationId: Long,
			override val token: String,
			override val data: ApplicationCommandData,
		) : InteractionWithData {
			override val type = APPLICATION_COMMAND_AUTOCOMPLETE
		}

		data class ModalSubmit(
			override val id: Long,
			override val applicationId: Long,
			override val token: String,
			override val data: ModalSubmitData,
		) : InteractionWithData {
			override val type = MODAL_SUBMIT
		}

	}
}

data class UntypedInteractionObject(
	val id: Long,
	val applicationId: Long,
	val token: String,
	val type: InteractionType,
	val data: InteractionData?,
) {

	fun typed(): InteractionObject {
		return when (type) {
			PING -> Ping(id, applicationId, token)
			APPLICATION_COMMAND -> InteractionWithData.ApplicationCommand(id, applicationId, token, data!! as ApplicationCommandData)
			MESSAGE_COMPONENT -> InteractionWithData.MessageComponent(id, applicationId, token, data!! as MessageComponentData)
			APPLICATION_COMMAND_AUTOCOMPLETE -> InteractionWithData.Autocomplete(id, applicationId, token, data!! as ApplicationCommandData)
			MODAL_SUBMIT -> InteractionWithData.ModalSubmit(id, applicationId, token, data!! as ModalSubmitData)
		}
	}
}
