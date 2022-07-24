package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackDataMessage
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.CHANNEL_MESSAGE_WITH_SOURCE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import org.springframework.stereotype.Service

@Service
class ApplicationCommandService {

	fun onApplicationCommand(interactionObject: InteractionObject): InteractionResponse {
		return InteractionResponse(CHANNEL_MESSAGE_WITH_SOURCE, InteractionCallbackDataMessage(content = "henlo"))
	}

}
