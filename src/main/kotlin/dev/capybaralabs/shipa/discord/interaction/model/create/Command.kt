package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse

class Command(
	val create: CreateCommand,
	val handle: (InteractionObject) -> InteractionResponse
) {
	fun name(): String {
		return create.name
	}
}
