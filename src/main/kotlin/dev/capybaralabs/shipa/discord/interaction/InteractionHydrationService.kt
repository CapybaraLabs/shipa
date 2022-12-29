package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import org.springframework.stereotype.Service

/**
 * Create interaction state from interactions saved in the repo. Caution: The state is likely to be wrong,
 * and divert further due to concurrent usage. Callers need to ensure the correct operations are performed upon the state.
 */
interface InteractionHydrationService {

	suspend fun hydrate(interactionId: Long): InteractionStateHolder?

	suspend fun hydrateAll(interactionIds: Collection<Long>): List<InteractionStateHolder>
}

@Service
private class InteractionHydrationServiceImpl(
	private val interactionRepository: InteractionRepository,
	private val unifiedInteractionService: UnifiedInteractionService,
) : InteractionHydrationService {

	override suspend fun hydrate(interactionId: Long): InteractionStateHolder? {
		return interactionRepository.find(interactionId)?.let { hydrateInteraction(it) }
	}

	override suspend fun hydrateAll(interactionIds: Collection<Long>): List<InteractionStateHolder> {
		val interactions = interactionRepository.findAll(interactionIds)

		return interactions.mapNotNull { hydrateInteraction(it) }
	}

	private suspend fun hydrateInteraction(interaction: InteractionWithData): InteractionStateHolder? {
		return unifiedInteractionService.get(interaction)
	}
}
