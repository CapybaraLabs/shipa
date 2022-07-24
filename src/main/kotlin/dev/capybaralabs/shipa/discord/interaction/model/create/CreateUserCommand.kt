package dev.capybaralabs.shipa.discord.interaction.model.create

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.USER

data class CreateUserCommand(
	val name: String,
) : CreateCommand(USER)
