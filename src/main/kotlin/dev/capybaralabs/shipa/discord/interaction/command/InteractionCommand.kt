package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.interaction.InteractionState
import dev.capybaralabs.shipa.discord.interaction.InteractionState.ApplicationCommandState.ApplicationCommandStateHolder
import dev.capybaralabs.shipa.discord.interaction.InteractionState.AutocompleteState.AutocompleteStateHolder
import dev.capybaralabs.shipa.discord.interaction.InteractionState.MessageComponentState.MessageComponentStateHolder
import dev.capybaralabs.shipa.discord.interaction.InteractionState.ModalState.ModalStateHolder
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

	fun onApplicationCommand(stateHolder: ApplicationCommandStateHolder) {
		stateHolder.reply(Message(content = "The dog ate my interaction handler."))
	}

	fun onMessageComponent(stateHolder: MessageComponentStateHolder) {
		stateHolder.reply(Message(content = "The dog ate my interaction handler."))
	}

	fun onAutocomplete(stateHolder: AutocompleteStateHolder) {
//		state.reply(Message(content = "The dog ate my interaction handler."))
	}

	fun onModalSubmit(stateHolder: ModalStateHolder) {
//		state.reply(Message(content = "The dog ate my interaction handler."))
	}

	fun onInteraction(stateHolder: InteractionState.InteractionStateHolder<*>) {
		when (stateHolder) {
			is ApplicationCommandStateHolder -> onApplicationCommand(stateHolder)
			is MessageComponentStateHolder -> onMessageComponent(stateHolder)
			is AutocompleteStateHolder -> onAutocomplete(stateHolder)
			is ModalStateHolder -> onModalSubmit(stateHolder)
		}
	}
}
