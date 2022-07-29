package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage

interface Command {
	val create: CreateCommand
	val onApplicationCommand: ((InteractionObject) -> InteractionResponse)?
	val onMessageComponent: ((InteractionObject) -> InteractionResponse)?
	val onAutocomplete: ((InteractionObject) -> InteractionResponse)?
	val onModalSubmit: ((InteractionObject) -> InteractionResponse)?

	fun name(): String {
		return create.name
	}

	fun onApplicationCommand(interaction: InteractionObject): InteractionResponse {
		return onApplicationCommand?.invoke(interaction) ?: SendMessage(Message(content = "Cannot handle input")) // TODO log error
	}

	fun onMessageComponent(interaction: InteractionObject): InteractionResponse {
		return onMessageComponent?.invoke(interaction) ?: SendMessage(Message(content = "Cannot handle input")) // TODO log error
	}

	fun onAutocomplete(interaction: InteractionObject): InteractionResponse {
		return onAutocomplete?.invoke(interaction) ?: SendMessage(Message(content = "Cannot handle input")) // TODO log error
	}

	fun onModalSubmit(interaction: InteractionObject): InteractionResponse {
		return onModalSubmit?.invoke(interaction) ?: SendMessage(Message(content = "Cannot handle input")) // TODO log error
	}

	class Stub(
		override val create: CreateCommand,
		override val onApplicationCommand: ((InteractionObject) -> InteractionResponse)? = null,
		override val onMessageComponent: ((InteractionObject) -> InteractionResponse)? = null,
		override val onAutocomplete: ((InteractionObject) -> InteractionResponse)? = null,
		override val onModalSubmit: ((InteractionObject) -> InteractionResponse)? = null,
	) : Command
}
