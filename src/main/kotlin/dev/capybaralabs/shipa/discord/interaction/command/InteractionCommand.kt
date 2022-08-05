package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.interaction.InteractionState
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand

interface InteractionCommand {
	fun creation(): CreateCommand

	fun name(): String {
		return creation().name
	}

	fun staticCustomIds(): List<String> {
		return listOf()
	}

	fun onApplicationCommand(received: InteractionState.ApplicationCommandState.Received) {
		received.reply(Message(content = "The dog ate my interaction handler."))
	}

	fun onMessageComponent(received: InteractionState.MessageComponentState.Received) {
		received.reply(Message(content = "The dog ate my interaction handler."))
	}

	fun onAutocomplete(received: InteractionState.AutocompleteState.Received) {
//		state.reply(Message(content = "The dog ate my interaction handler."))
	}

	fun onModalSubmit(received: InteractionState.ModalState.Received) {
//		state.reply(Message(content = "The dog ate my interaction handler."))
	}

	fun onInteraction(interactionState: InteractionState) {
		when (interactionState) {
			is InteractionState.ApplicationCommandState -> onApplicationCommand(interactionState as InteractionState.ApplicationCommandState.Received)
			is InteractionState.MessageComponentState -> onMessageComponent(interactionState as InteractionState.MessageComponentState.Received)
			is InteractionState.AutocompleteState -> onAutocomplete(interactionState as InteractionState.AutocompleteState.Received)
			is InteractionState.ModalState -> onModalSubmit(interactionState as InteractionState.ModalState.Received)
		}
	}
}
