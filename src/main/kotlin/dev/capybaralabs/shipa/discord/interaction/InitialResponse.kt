package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.future.await

class InitialResponse(
	// Complete with the initial result that we should respond to the Discord's interaction webhook.
	private val result: CompletableDeferred<InteractionResponse>,
	// Best effort estimation whether we already responded to Discord's interaction webhook. await on it before sending rest requests to minimize Unknown Webhook errors.
	private val resultSent: CompletableFuture<Unit>,
) {

	fun complete(value: InteractionResponse) {
		result.complete(value)
	}

	fun completeExceptionally(exception: Throwable) {
		result.completeExceptionally(exception)
	}

	val isCompleted: Boolean
		get() = result.isCompleted


	suspend fun awaitSent() {
		resultSent.await()
	}
}
