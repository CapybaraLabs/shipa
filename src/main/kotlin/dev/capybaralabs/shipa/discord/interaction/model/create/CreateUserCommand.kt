package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.USER

data class CreateUserCommand(
	override val name: String,
) : CreateCommand(USER, name)
