package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.interaction.InteractionRestService
import dev.capybaralabs.shipa.discord.interaction.InteractionState
import dev.capybaralabs.shipa.discord.interaction.InteractionState.ApplicationCommandState.ApplicationCommandStateHolder
import dev.capybaralabs.shipa.discord.interaction.InteractionState.AutocompleteState.AutocompleteStateHolder
import dev.capybaralabs.shipa.discord.interaction.InteractionState.InteractionStateHolder
import dev.capybaralabs.shipa.discord.interaction.InteractionState.MessageComponentState.MessageComponentStateHolder
import dev.capybaralabs.shipa.discord.interaction.InteractionState.ModalState.ModalStateHolder
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

		val interactionStateHolder: InteractionStateHolder<*> = when (interaction) {
			is InteractionWithData.ApplicationCommand -> ApplicationCommandStateHolder(InteractionState.ApplicationCommandState.received(interaction, result, restService))
			is InteractionWithData.MessageComponent -> MessageComponentStateHolder(InteractionState.MessageComponentState.received(interaction, result, restService))
			is InteractionWithData.Autocomplete -> AutocompleteStateHolder(InteractionState.AutocompleteState.received(interaction))
			is InteractionWithData.ModalSubmit -> ModalStateHolder(InteractionState.ModalState.received(interaction))
		}

		command?.onInteraction(interactionStateHolder)
			?: logger().warn("Unknown Command {}", interaction)
	}

}
