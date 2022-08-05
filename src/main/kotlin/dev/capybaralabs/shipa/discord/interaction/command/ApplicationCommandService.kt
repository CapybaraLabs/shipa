package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.interaction.InteractionRestService
import dev.capybaralabs.shipa.discord.interaction.InteractionState
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.logger
import java.util.concurrent.CompletableFuture
import org.springframework.stereotype.Service

@Service
class ApplicationCommandService(
	private val commandLookupService: CommandLookupService,
	private val restService: InteractionRestService,
) {

	fun onInteraction(interaction: InteractionWithData, result: CompletableFuture<InteractionResponse>) {
		val command = when (interaction) {
			is InteractionWithData.ApplicationCommand -> commandLookupService.findByName(interaction.data.name)
			is InteractionWithData.MessageComponent -> commandLookupService.findByCustomId(interaction.data.customId)
			is InteractionWithData.Autocomplete -> commandLookupService.findByName(interaction.data.name)
			is InteractionWithData.ModalSubmit -> commandLookupService.findByCustomId(interaction.data.customId)
		}

		val interactionState = when (interaction) {
			is InteractionWithData.ApplicationCommand -> InteractionState.ApplicationCommandState.received(interaction, result, restService)
			is InteractionWithData.MessageComponent -> InteractionState.MessageComponentState.received(interaction, result, restService)
			is InteractionWithData.Autocomplete -> InteractionState.AutocompleteState.received(interaction)
			is InteractionWithData.ModalSubmit -> InteractionState.ModalState.received(interaction)
		}

		command?.onInteraction(interactionState)
			?: logger().warn("Unknown Command {}", interaction)
	}

}
