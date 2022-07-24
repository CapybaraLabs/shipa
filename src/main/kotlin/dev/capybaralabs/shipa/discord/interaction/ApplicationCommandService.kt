package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackDataMessage
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.CHANNEL_MESSAGE_WITH_SOURCE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.create.Command
import org.springframework.stereotype.Service

@Service
class ApplicationCommandService(
	private val commands: List<Command>
) {

	fun onApplicationCommand(interactionObject: InteractionObject): InteractionResponse {
		val interactionName = interactionObject.interactionData!!
			.let { it as ApplicationCommandData }
			.name

		return commands.find { it.name() == interactionName }?.handle?.invoke(interactionObject)
			?: InteractionResponse(CHANNEL_MESSAGE_WITH_SOURCE, InteractionCallbackDataMessage(content = "Unknown Command"))
	}

}
