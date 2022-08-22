package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import org.springframework.stereotype.Service

/**
 * Create interaction state from interactions saved in the repo. Caution: The state is likely to be wrong,
 * and divert further due to concurrent usage. Callers need to ensure the correct operations are performed upon the state.
 * Sending new messages and editing the original message should be fine mostly, hence they are exposed.
 */
@Service
class InteractionHydrationService(
	private val interactionRepository: InteractionRepository,
	private val restService: InteractionRestService,
) {

	suspend fun hydrate(interactionId: Long): HydratedState? {
		return interactionRepository.find(interactionId)?.let { hydrateInteraction(it) }
	}

	suspend fun hydrateAll(interactionIds: Collection<Long>): List<HydratedState> {
		val interactions = interactionRepository.findAll(interactionIds)

		return interactions.map { hydrateInteraction(it) }
	}

	private fun hydrateInteraction(interaction: InteractionWithData): HydratedState {
		return HydratedState(interaction, restService)
	}

	class HydratedState(
		private val interaction: InteractionWithData,
		private val restService: InteractionRestService,
	) : InteractionState {

		override fun interaction(): InteractionWithData {
			return interaction
		}

		suspend fun reply(message: InteractionCallbackData.Message) {
			restService.createFollowupMessage(interaction().token, message)
		}

		suspend fun edit(message: InteractionCallbackData.Message) {
			restService.editOriginalResponse(interaction().token, message)
		}
	}
}
