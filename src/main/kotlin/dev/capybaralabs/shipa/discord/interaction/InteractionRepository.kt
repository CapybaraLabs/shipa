package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData

/**
 * Save all incoming interactions for later use to e.g. update messages by other interactions or other sources of invocations.
 * Implementations need to take care of timeouts, e.g. by using Redis' Expiration Feature
 */
interface InteractionRepository {

	suspend fun save(interaction: InteractionWithData)

	suspend fun find(interactionId: Long): InteractionWithData?

	/**
	 * @return all items that could be found, may be empty or missing items.
	 */
	suspend fun findAll(interactionIds: Collection<Long>): List<InteractionWithData>
}
