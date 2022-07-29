package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage
import dev.capybaralabs.shipa.discord.interaction.model.create.Command
import org.springframework.stereotype.Service

@Service
class ApplicationCommandService(
	private val commands: List<Command>
) {

	fun onApplicationCommand(interactionObject: InteractionObject): InteractionResponse {
		val interactionName = interactionObject.data!!
			.let { it as ApplicationCommandData }
			.name

		return commands.find { it.name() == interactionName }?.onApplicationCommand(interactionObject)
			?: SendMessage(Message(content = "Unknown Command"))
	}

	fun onMessageComponent(interactionObject: InteractionObject): InteractionResponse {
		val interactionName = interactionObject.data!!
			.let { it as ApplicationCommandData }
			.name

		return commands.find { it.name() == interactionName }?.onMessageComponent(interactionObject)
			?: SendMessage(Message(content = "Unknown Command"))
	}

	fun onAutocomplete(interactionObject: InteractionObject): InteractionResponse {
		val interactionName = interactionObject.data!!
			.let { it as ApplicationCommandData }
			.name

		return commands.find { it.name() == interactionName }?.onAutocomplete(interactionObject)
			?: SendMessage(Message(content = "Unknown Command"))
	}

	fun onModalSubmit(interactionObject: InteractionObject): InteractionResponse {
		val interactionName = interactionObject.data!!
			.let { it as ApplicationCommandData }
			.name

		return commands.find { it.name() == interactionName }?.onModalSubmit(interactionObject)
			?: SendMessage(Message(content = "Unknown Command"))
	}

}
