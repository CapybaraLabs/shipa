@file:Suppress("DeferredIsResult") // actor usage is intended to be opt-in blocking.

package dev.capybaralabs.shipa.discord.interaction

import com.github.benmanes.caffeine.cache.Caffeine
import dev.capybaralabs.shipa.discord.delay
import dev.capybaralabs.shipa.discord.delayUntil
import dev.capybaralabs.shipa.discord.interaction.AutoAckTactic.ACK
import dev.capybaralabs.shipa.discord.interaction.AutoAckTactic.ACK_EPHEMERAL
import dev.capybaralabs.shipa.discord.interaction.AutoAckTactic.DO_NOTHING
import dev.capybaralabs.shipa.discord.interaction.InteractionResponseActionResult
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.Ack
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.Autocomplete
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.CompleteOrEdit
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.CompleteOrFollowup
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.Delete
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.Edit
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.Fetch
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.Followup
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionMsg.ShowModal
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallback
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallback.Flags
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallback.Modal
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.MessageComponent
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.OptionChoice
import dev.capybaralabs.shipa.discord.millis
import dev.capybaralabs.shipa.discord.minutes
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.Message
import dev.capybaralabs.shipa.discord.model.MessageFlag.EPHEMERAL
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException
import kotlin.Boolean
import kotlin.Exception
import kotlin.IllegalStateException
import kotlin.Long
import kotlin.OptIn
import kotlin.Suppress
import kotlin.let
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import dev.capybaralabs.shipa.discord.interaction.InteractionResponseActionResult as Result


val INTERACTION_TIMEOUT = 15.minutes

class UnifiedInteraction {
	companion object {
		val log: Logger = LoggerFactory.getLogger(UnifiedInteraction::class.java)
	}
}


/**
 * This class does all at once
 * - define the public API for end users for ALL interaction types
 * - state protection, via single coroutine concurrency (actor used in impl)
 * - timeout & auto-ack behaviour
 *
 */
interface InteractionStateHolder {

	val interaction: InteractionWithData

	/**
	 * Ack this interaction
	 */
	fun ack(ephemeral: Boolean = true): Deferred<Result.Acked>

	/**
	 * Immediately complete this interaction with the response. If it has been completed already, edit the existing original message.
	 */
	fun completeOrEditOriginal(message: InteractionCallback.Message): Deferred<Result.CompletedOrWithMessage>

	/**
	 * Immediately complete this interaction with the response. If it has been completed already, send a followup message.
	 */
	fun completeOrFollowup(message: InteractionCallback.Message): Deferred<Result.CompletedOrWithMessage>

	/**
	 * Create a followup message. If this interaction has not been acked yet, will auto-ack with the ephemeral settings of the passed message by default.
	 * @throws IllegalStateException if this interaction has not been acked yet and [autoAck] is set to false.
	 */
	fun followup(followup: InteractionCallback.FollowupMessage, autoAck: Boolean = true): Deferred<Result.FollowedUp>

	/**
	 * Fetch the original message.
	 * @throws IllegalStateException if this interaction has not been acked yet
	 */
	fun fetchOriginal(): Deferred<Result.Fetched>

	/**
	 * Fetch a followup message.
	 * @throws IllegalStateException if this interaction has not been acked yet
	 */
	fun fetchFollowup(messageId: Long): Deferred<Result.Fetched>

	/**
	 * Edit the original message.
	 * @throws IllegalStateException if this interaction has not been acked yet
	 */
	fun editOriginal(followup: InteractionCallback.FollowupMessage): Deferred<Result.Edited>

	/**
	 * Edit a followup message.
	 * @throws IllegalStateException if this interaction has not been acked yet
	 */
	fun editFollowup(messageId: Long, followup: InteractionCallback.FollowupMessage): Deferred<Result.Edited>

	/**
	 * Delete the original message.
	 * @throws IllegalStateException if this interaction has not been acked yet
	 */
	fun deleteOriginal(): Deferred<Result.Deleted>

	/**
	 * Delete a followup message.
	 * @throws IllegalStateException if this interaction has not been acked yet
	 */
	fun deleteFollowup(messageId: Long): Deferred<Result.Deleted>

	/**
	 * Respond to an autocomplete interaction with a bunch of choices
	 */
	fun autocomplete(choices: List<OptionChoice>): Deferred<Result.Completed>

	/**
	 * Show a modal to the user
	 */
	fun showModal(modal: Modal): Deferred<Result.Completed>
}

enum class AutoAckTactic {
	DO_NOTHING,
	ACK_EPHEMERAL,
	ACK
}

sealed interface InteractionResponseActionResult {

	sealed interface CompletedOrWithMessage : InteractionResponseActionResult
	sealed interface WithMessage : InteractionResponseActionResult, CompletedOrWithMessage {
		val message: Message
	}

	data object Acked : InteractionResponseActionResult
	data object Completed : InteractionResponseActionResult, CompletedOrWithMessage
	data class FollowedUp(override val message: Message) : WithMessage

	data class Fetched(override val message: Message) : WithMessage
	data class Edited(override val message: Message) : WithMessage
	data object Deleted : InteractionResponseActionResult
}

private sealed interface UnifiedInteractionMsg<E : Result> {

	val response: CompletableDeferred<E>

	data class Ack(
		val ephemeral: Boolean,
		override val response: CompletableDeferred<Result.Acked>,
	) : UnifiedInteractionMsg<Result.Acked>

	data class CompleteOrEdit(
		val message: InteractionCallback.Message,
		val messageId: Long?,
		override val response: CompletableDeferred<Result.CompletedOrWithMessage>,
	) : UnifiedInteractionMsg<Result.CompletedOrWithMessage>

	data class CompleteOrFollowup(
		val message: InteractionCallback.Message,
		override val response: CompletableDeferred<Result.CompletedOrWithMessage>,
	) : UnifiedInteractionMsg<Result.CompletedOrWithMessage>

	data class Edit(
		val messageId: Long?,
		val followup: InteractionCallback.FollowupMessage,
		override val response: CompletableDeferred<Result.Edited>,
	) : UnifiedInteractionMsg<Result.Edited>

	data class Followup(
		val followup: InteractionCallback.FollowupMessage,
		val autoAck: Boolean,
		override val response: CompletableDeferred<Result.FollowedUp>,
	) : UnifiedInteractionMsg<Result.FollowedUp>

	data class Fetch(
		val messageId: Long?,
		override val response: CompletableDeferred<Result.Fetched>,
	) : UnifiedInteractionMsg<Result.Fetched>

	data class Delete(
		val messageId: Long?,
		override val response: CompletableDeferred<Result.Deleted>,
	) : UnifiedInteractionMsg<Result.Deleted>

	data class Autocomplete(
		val choices: List<OptionChoice>,
		override val response: CompletableDeferred<Result.Completed>,
	) : UnifiedInteractionMsg<Result.Completed>

	data class ShowModal(
		val modal: Modal,
		override val response: CompletableDeferred<Result.Completed>,
	) : UnifiedInteractionMsg<Result.Completed>

}


@Service
internal class UnifiedInteractionService(
	private val restService: InteractionRestService,
	private val interactionScope: CoroutineScope, // do we need a dedicated scope?
) {

	private val actors = Caffeine.newBuilder()
		.expireAfterWrite(INTERACTION_TIMEOUT)
		.build<Long, InteractionStateHolderImpl>()

	suspend fun get(interaction: InteractionWithData): InteractionStateHolder? {
		return actors.getIfPresent(interaction.id)
	}

	suspend fun create(interaction: InteractionWithData, autoAckTactic: AutoAckTactic, initialResponse: InitialResponse): InteractionStateHolder {
		UnifiedInteraction.log.trace("Interaction {}: Create State", interaction.id)
		val state = UnifiedInteractionState(interaction, initialResponse, restService)
		UnifiedInteraction.log.trace("Interaction {}: Create Actor", interaction.id)
		val actor = interactionScope.unifiedInteractionActor(state)
		UnifiedInteraction.log.trace("Interaction {}: Create State Holder", interaction.id)
		val stateHolder = InteractionStateHolderImpl(actor, interactionScope, interaction, autoAckTactic)
		UnifiedInteraction.log.trace("Interaction {}: Save Actor", interaction.id)
		actors.put(interaction.id, stateHolder)
		UnifiedInteraction.log.trace("Interaction {}: Create Done", interaction.id)
		return stateHolder
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	private fun CoroutineScope.unifiedInteractionActor(state: UnifiedInteractionState) = actor<UnifiedInteractionMsg<*>> {
		for (msg in channel) {
			try {
				withTimeout(30.seconds) {
					when (msg) {
						is Ack -> state.ack(msg.ephemeral).let { msg.response.complete(Result.Acked) }
						is CompleteOrEdit -> msg.response.complete(state.completeOrEdit(msg.message))
						is CompleteOrFollowup -> msg.response.complete(state.completeOrFollowup(msg.message))
						is Edit -> msg.response.complete(state.edit(msg.messageId, msg.followup))
						is Followup -> msg.response.complete(state.followup(msg.followup, msg.autoAck))
						is Fetch -> msg.response.complete(state.fetch(msg.messageId))
						is Delete -> msg.response.complete(state.delete(msg.messageId))
						is Autocomplete -> state.autocomplete(msg.choices).let { msg.response.complete(Result.Completed) }
						is ShowModal -> state.showModal(msg.modal).let { msg.response.complete(Result.Completed) }
					}
				}
			} catch (e: TimeoutCancellationException) {
				if (!msg.response.isCompleted) {
					msg.response.completeExceptionally(TimeoutException("Interaction actor did not process message $msg in time!")) // can we find a way to propagate the cause?
				} else {
					UnifiedInteraction.log.warn("Caught exception but response already completed.", e)
				}
			} catch (e: Exception) {
				if (!msg.response.isCompleted) {
					msg.response.completeExceptionally(e)
				} else {
					UnifiedInteraction.log.warn("Caught exception but response already completed.", e)
				}
			} finally {
				if (!msg.response.isCompleted) {
					msg.response.completeExceptionally(IllegalStateException("Response was not completed by actor."))
				}
			}
		}
	}
}

private class InteractionStateHolderImpl(
	private val actor: SendChannel<UnifiedInteractionMsg<*>>,
	private val scope: CoroutineScope,
	override val interaction: InteractionWithData,
	private val autoAckTactic: AutoAckTactic,
) : InteractionStateHolder {

	companion object {
		private val AUTO_ACK_DELAY = 2_500.millis
	}

	init {
		UnifiedInteraction.log.trace("Interaction {}: Init state holder", interaction.id)
		scope.async {
			val alreadyDelayed = Duration.between(interaction.shipaMetadata.received, Instant.now())
			val adjustedDelay = AUTO_ACK_DELAY.minus(alreadyDelayed).coerceIn(Duration.ZERO, AUTO_ACK_DELAY)
			UnifiedInteraction.log.trace("Interaction {}: Starting auto-ack delay of {}", interaction.id, adjustedDelay)
			delay(adjustedDelay)
			UnifiedInteraction.log.trace("Interaction {}: Auto-ack delayed, tactic is {}", interaction.id, autoAckTactic)
			when (autoAckTactic) {
				DO_NOTHING -> {}
				ACK_EPHEMERAL -> ack(true).await()
				ACK -> ack(false).await()
			}
			UnifiedInteraction.log.trace("Interaction {}: Auto-ack tactic {} applied", interaction.id, autoAckTactic)

			delayUntil(interaction.shipaMetadata.received.plus(INTERACTION_TIMEOUT))
			actor.close()
		}.invokeOnCompletion {
			UnifiedInteraction.log.debug("Interaction {}: Closed actor", interaction.id)
		}
	}

	private fun send(msg: UnifiedInteractionMsg<*>) {
		scope.async {
			actor.send(msg)
		}.invokeOnCompletion { error ->
			error?.let {
				if (error !is CancellationException) {
					val completed = msg.response.completeExceptionally(error)
					if (!completed) {
						UnifiedInteraction.log.warn("Potentially swallowed error", error)
					}
				}
			}
		}
	}

	override fun ack(ephemeral: Boolean): Deferred<Result.Acked> {
		val response = CompletableDeferred<Result.Acked>()
		send(Ack(ephemeral, response))
		return response
	}

	override fun completeOrEditOriginal(message: InteractionCallback.Message): Deferred<Result.CompletedOrWithMessage> {
		val response = CompletableDeferred<Result.CompletedOrWithMessage>()
		send(CompleteOrEdit(message, null, response))
		return response
	}

	override fun completeOrFollowup(message: InteractionCallback.Message): Deferred<Result.CompletedOrWithMessage> {
		val response = CompletableDeferred<Result.CompletedOrWithMessage>()
		send(CompleteOrFollowup(message, response))
		return response
	}

	override fun followup(followup: InteractionCallback.FollowupMessage, autoAck: Boolean): Deferred<Result.FollowedUp> {
		val response = CompletableDeferred<Result.FollowedUp>()
		send(Followup(followup, autoAck, response))
		return response
	}

	override fun editOriginal(followup: InteractionCallback.FollowupMessage): Deferred<Result.Edited> {
		val response = CompletableDeferred<Result.Edited>()
		send(Edit(null, followup, response))
		return response
	}

	override fun editFollowup(messageId: Long, followup: InteractionCallback.FollowupMessage): Deferred<Result.Edited> {
		val response = CompletableDeferred<Result.Edited>()
		send(Edit(messageId, followup, response))
		return response

	}

	override fun fetchOriginal(): Deferred<Result.Fetched> {
		val response = CompletableDeferred<Result.Fetched>()
		send(Fetch(null, response))
		return response
	}

	override fun fetchFollowup(messageId: Long): Deferred<Result.Fetched> {
		val response = CompletableDeferred<Result.Fetched>()
		send(Fetch(messageId, response))
		return response

	}

	override fun deleteOriginal(): Deferred<Result.Deleted> {
		val response = CompletableDeferred<Result.Deleted>()
		send(Delete(null, response))
		return response
	}

	override fun deleteFollowup(messageId: Long): Deferred<Result.Deleted> {
		val response = CompletableDeferred<Result.Deleted>()
		send(Delete(messageId, response))
		return response
	}

	override fun autocomplete(choices: List<OptionChoice>): Deferred<Result.Completed> {
		val response = CompletableDeferred<Result.Completed>()
		send(Autocomplete(choices, response))
		return response
	}

	override fun showModal(modal: Modal): Deferred<Result.Completed> {
		val response = CompletableDeferred<Result.Completed>()
		send(ShowModal(modal, response))
		return response
	}
}


private class UnifiedInteractionState(
	val interaction: InteractionWithData,
	private val initialResponse: InitialResponse,
	private val restService: InteractionRestService,
) {
	fun ack(ephemeral: Boolean) {
		if (initialResponse.isCompleted) {
			return
		}

		val ack = if (interaction is MessageComponent) {
			InteractionResponse.AckUpdate
		} else if (ephemeral) {
			InteractionResponse.Ack(Flags(IntBitfield.of(EPHEMERAL)))
		} else {
			InteractionResponse.Ack()
		}

		initialResponse.complete(ack) // race condition from timeouts / auto acks outside of this actor?
	}

	suspend fun completeOrEdit(message: InteractionCallback.Message): Result.CompletedOrWithMessage {
		if (!initialResponse.isCompleted) {
			val response = if (interaction is MessageComponent) {
				InteractionResponse.UpdateMessage(message)
			} else {
				InteractionResponse.SendMessage(message)
			}
			initialResponse.complete(response)
			return Result.Completed
		}

		initialResponse.awaitSent()
		val followup = InteractionCallback.FollowupMessage(message)
		return Result.Edited(restService.editOriginalResponse(interaction.token, followup))
	}

	suspend fun completeOrFollowup(message: InteractionCallback.Message): Result.CompletedOrWithMessage {
		if (!initialResponse.isCompleted) {
			val response = if (interaction is MessageComponent) {
				InteractionResponse.UpdateMessage(message)
			} else {
				InteractionResponse.SendMessage(message)
			}
			initialResponse.complete(response)
			return Result.Completed
		}

		initialResponse.awaitSent()
		val followup = InteractionCallback.FollowupMessage(message)
		return Result.FollowedUp(restService.createFollowupMessage(interaction.token, followup))
	}

	suspend fun followup(followup: InteractionCallback.FollowupMessage, autoAck: Boolean): Result.FollowedUp {
		if (!initialResponse.isCompleted) {
			if (autoAck) {
				ack(followup.message.flags?.contains(EPHEMERAL) ?: true)
			} else {
				throw IllegalStateException("Can't send followup on un-acked interaction")
			}
		}

		initialResponse.awaitSent()
		return Result.FollowedUp(restService.createFollowupMessage(interaction.token, followup))
	}

	suspend fun fetch(messageId: Long?): Result.Fetched {
		if (!initialResponse.isCompleted) {
			throw IllegalStateException("Can't fetch on un-acked interaction") // but maybe message components or modals can?
		}

		initialResponse.awaitSent()
		val fetched = if (messageId != null) {
			restService.fetchFollowupMessage(interaction.token, messageId)
		} else {
			restService.fetchOriginalResponse(interaction.token)
		}
		return Result.Fetched(fetched)
	}

	suspend fun edit(messageId: Long?, followup: InteractionCallback.FollowupMessage): Result.Edited {
		if (!initialResponse.isCompleted) {
			throw IllegalStateException("Can't edit on un-acked interaction") // but maybe message components or modals can?
		}
		initialResponse.awaitSent()
		val edited = if (messageId != null) {
			restService.editFollowupMessage(interaction.token, followup, messageId)
		} else {
			restService.editOriginalResponse(interaction.token, followup)
		}

		return Result.Edited(edited)
	}

	suspend fun delete(messageId: Long?): Result.Deleted {
		if (!initialResponse.isCompleted) {
			throw IllegalStateException("Can't delete on un-acked interaction") // but maybe message components or modals can?
		}

		initialResponse.awaitSent()
		if (messageId != null) {
			restService.deleteFollowupMessage(interaction.token, messageId)
		} else {
			restService.deleteOriginalResponse(interaction.token)
		}
		return Result.Deleted
	}

	fun autocomplete(choices: List<OptionChoice>) {
		if (initialResponse.isCompleted) {
			return
		}

		initialResponse.complete(InteractionResponse.Autocomplete(InteractionCallback.Autocomplete(choices)))
	}

	fun showModal(modal: Modal) {
		if (initialResponse.isCompleted) {
			UnifiedInteraction.log.warn("Cannot send Modal to acked interaction")
			return
		}

		initialResponse.complete(InteractionResponse.Modal(modal))
	}

}
