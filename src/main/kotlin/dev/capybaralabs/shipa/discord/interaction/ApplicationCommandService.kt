package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage
import org.springframework.stereotype.Service

@Service
class ApplicationCommandService(
	private val commandLookupService: CommandLookupService,
) {

	fun onApplicationCommand(interactionObject: InteractionObject): InteractionResponse {
		val interactionName = interactionObject.data!!
			.let { it as InteractionData.ApplicationCommandData }
			.name

		return commandLookupService.findByName(interactionName)
			?.onApplicationCommand(interactionObject)
			?: SendMessage(Message(content = "Unknown Command"))
	}

	fun onMessageComponent(interactionObject: InteractionObject): InteractionResponse {
		val customId = interactionObject.data!!
			.let { it as InteractionData.MessageComponentData }
			.customId

		return commandLookupService.findByCustomId(customId)?.onMessageComponent(interactionObject)
			?: SendMessage(Message(content = "Unknown Command"))
	}

	fun onAutocomplete(interactionObject: InteractionObject): InteractionResponse {
		val interactionName = interactionObject.data!!
			.let { it as InteractionData.ApplicationCommandData }
			.name

		return commandLookupService.findByName(interactionName)?.onAutocomplete(interactionObject)
			?: SendMessage(Message(content = "Unknown Command"))
	}

	fun onModalSubmit(interactionObject: InteractionObject): InteractionResponse {
		val customId = interactionObject.data!!
			.let { it as InteractionData.ModalSubmitData }
			.customId

		return commandLookupService.findByCustomId(customId)?.onModalSubmit(interactionObject)
			?: SendMessage(Message(content = "Unknown Command"))
	}

}
