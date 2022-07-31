package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage
import dev.capybaralabs.shipa.logger

interface Command {
	val create: CreateCommand
	val onApplicationCommand: ((InteractionWithData.ApplicationCommand) -> InteractionResponse)?
	val onMessageComponent: ((InteractionWithData.MessageComponent) -> InteractionResponse)?
	val onAutocomplete: ((InteractionWithData.Autocomplete) -> InteractionResponse)?
	val onModalSubmit: ((InteractionWithData.ModalSubmit) -> InteractionResponse)?

	fun name(): String {
		return create.name
	}

	fun staticCustomIds(): List<String> {
		return listOf()
	}

	fun onInteraction(interaction: InteractionWithData): InteractionResponse {
		val possibleResponse = when (interaction) {
			is InteractionWithData.ApplicationCommand -> onApplicationCommand?.invoke(interaction)
			is InteractionWithData.MessageComponent -> onMessageComponent?.invoke(interaction)
			is InteractionWithData.Autocomplete -> onAutocomplete?.invoke(interaction)
			is InteractionWithData.ModalSubmit -> onModalSubmit?.invoke(interaction)
		}

		if (possibleResponse == null) {
			logger().warn("Unhandled interaction $interaction")
			return SendMessage(Message(content = "The dog ate my interaction handler."))
		}
		return possibleResponse
	}

	class Stub(
		override val create: CreateCommand,
		override val onApplicationCommand: ((InteractionWithData.ApplicationCommand) -> InteractionResponse)? = null,
		override val onMessageComponent: ((InteractionWithData.MessageComponent) -> InteractionResponse)? = null,
		override val onAutocomplete: ((InteractionWithData.Autocomplete) -> InteractionResponse)? = null,
		override val onModalSubmit: ((InteractionWithData.ModalSubmit) -> InteractionResponse)? = null,
	) : Command
}
