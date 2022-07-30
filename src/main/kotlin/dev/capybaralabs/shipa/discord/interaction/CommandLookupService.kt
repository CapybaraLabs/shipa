package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.create.Command

interface CommandLookupService {

	fun findByName(name: String): Command?

	fun findByCustomId(customId: String): Command?

}
