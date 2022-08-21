package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.InteractionRepository
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import org.springframework.stereotype.Repository

@Repository
class InMemoryInteractionRepository : InteractionRepository {

	private val interactions: MutableMap<Long, InteractionWithData> = HashMap()

	override suspend fun save(interaction: InteractionWithData) {
		interactions[interaction.id] = interaction
	}

	override suspend fun find(interactionId: Long): InteractionWithData? {
		return interactions[interactionId]
	}

	override suspend fun findAll(interactionIds: Collection<Long>): List<InteractionWithData> {
		return interactionIds.mapNotNull { interactions[it] }
	}
}
