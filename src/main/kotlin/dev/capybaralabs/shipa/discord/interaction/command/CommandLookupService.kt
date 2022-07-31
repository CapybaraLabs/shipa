package dev.capybaralabs.shipa.discord.interaction.command

interface CommandLookupService {

	fun findByName(name: String): InteractionCommand?

	fun findByCustomId(customId: String): InteractionCommand?

}
