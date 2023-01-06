package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.discord.interaction.AutoAckTactic
import dev.capybaralabs.shipa.discord.interaction.AutoAckTactic.ACK_EPHEMERAL
import dev.capybaralabs.shipa.discord.interaction.InteractionStateHolder
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallback.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.Autocomplete
import dev.capybaralabs.shipa.discord.interaction.model.OptionChoice.StringChoice
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand

interface InteractionCommand {
	fun creation(): CreateCommand

	fun name(): String {
		return creation().name
	}

	fun staticCustomIds(): List<String> {
		return listOf()
	}

	fun autoAckTactic(): AutoAckTactic {
		return ACK_EPHEMERAL
	}

	suspend fun onInteraction(stateHolder: InteractionStateHolder) {
		when (stateHolder.interaction) {
			is Autocomplete -> stateHolder.autocomplete(listOf(StringChoice("Error", "The capybara ate my interaction handler."))).await()
			else -> stateHolder.completeOrEditOriginal(Message("The capybara ate my interaction handler.")).await()
		}
	}
}
