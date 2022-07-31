package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage
import org.springframework.stereotype.Service

@Service
class ApplicationCommandService(
	private val commandLookupService: CommandLookupService,
) {

	fun onInteraction(interaction: InteractionWithData): Sequence<InteractionResponse> {
		val command = when (interaction) {
			is InteractionWithData.ApplicationCommand -> commandLookupService.findByName(interaction.data.name)
			is InteractionWithData.MessageComponent -> commandLookupService.findByCustomId(interaction.data.customId)
			is InteractionWithData.Autocomplete -> commandLookupService.findByName(interaction.data.name)
			is InteractionWithData.ModalSubmit -> commandLookupService.findByCustomId(interaction.data.customId)
		}
		return command?.onInteraction(interaction)
			?: sequenceOf(SendMessage(Message(content = "Unknown Command")))
	}

}
