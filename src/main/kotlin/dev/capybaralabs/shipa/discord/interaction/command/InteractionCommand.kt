package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand
import dev.capybaralabs.shipa.logger

interface InteractionCommand {
	fun creation(): CreateCommand

	fun name(): String {
		return creation().name
	}

	fun staticCustomIds(): List<String> {
		return listOf()
	}

	fun onApplicationCommand(interaction: InteractionWithData.ApplicationCommand): InteractionResponse? {
		return null
	}

	fun onMessageComponent(interaction: InteractionWithData.MessageComponent): InteractionResponse? {
		return null
	}

	fun onAutocomplete(interaction: InteractionWithData.Autocomplete): InteractionResponse? {
		return null
	}

	fun onModalSubmit(interaction: InteractionWithData.ModalSubmit): InteractionResponse? {
		return null
	}

	fun onInteraction(interaction: InteractionWithData): InteractionResponse {
		val possibleResponse = when (interaction) {
			is InteractionWithData.ApplicationCommand -> onApplicationCommand(interaction)
			is InteractionWithData.MessageComponent -> onMessageComponent(interaction)
			is InteractionWithData.Autocomplete -> onAutocomplete(interaction)
			is InteractionWithData.ModalSubmit -> onModalSubmit(interaction)
		}

		if (possibleResponse == null) {
			logger().warn("Unhandled interaction $interaction")
			return SendMessage(Message(content = "The dog ate my interaction handler."))
		}
		return possibleResponse
	}
}