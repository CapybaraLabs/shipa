package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.ApplicationCommand
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.Autocomplete
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.MessageComponent
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.ModalSubmit
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.model.Message
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

internal open class Context<T : InteractionWithData>(
	val restService: InteractionRestService,
	val interaction: T,
) {
	internal fun token(): String = interaction.token
}

internal class ApplicationCommandContext(restService: InteractionRestService, interaction: ApplicationCommand) : Context<ApplicationCommand>(restService, interaction)
internal class MessageComponentContext(restService: InteractionRestService, interaction: MessageComponent) : Context<MessageComponent>(restService, interaction)


// TODO: how to (if necessary) model doneness / timeout
sealed class InteractionState {

	abstract fun interaction(): InteractionWithData

	private var used = AtomicBoolean(false)
	protected fun <T> checkUsed(func: () -> T): T {
		val success = used.compareAndSet(false, true)

		if (success) {
			return func.invoke()
		} else {
			throw IllegalStateException("This state has been used already, continue with the return value!")
		}
	}

	protected fun replaceOrAppend(messages: List<Message>, message: Message): List<Message> {
		val index = messages.indexOfFirst { it.id == message.id }
		return if (index < 0) {
			messages + message
		} else {
			messages.mapIndexed { i, it -> if (i == index) message else it }
		}
	}

	// marker interface for initial states
	sealed interface Initial

	// marker interface for all holders
	// holder are convenience classes that update their state in place but do not enforce method use through typesafety.
	// use the doXYZ methods on the concrete classes to take advantage of typesafety of possible actions but you will have to hold the state yourself.
	sealed interface InteractionStateHolder {
		// entry point for typesafe state chains
		fun getInitialState(): Initial
		fun getInteraction(): InteractionWithData
	}

	sealed interface ApplicationCommandState {

		companion object {
			fun received(interaction: ApplicationCommand, initial: CompletableFuture<InteractionResponse>, restService: InteractionRestService): Received {
				return Received(initial, ApplicationCommandContext(restService, interaction))
			}
		}

		fun ack(): Thinking {
			throw IllegalStateException("Cannot ack while in state ${javaClass.simpleName}")
		}

		fun reply(message: InteractionCallbackData.Message): MessageSent {
			throw IllegalStateException("Cannot reply while in state ${javaClass.simpleName}")
		}

		fun edit(message: InteractionCallbackData.Message, messageId: Long? = null): MessageSent {
			throw IllegalStateException("Cannot edit while in state ${javaClass.simpleName}")
		}

		// TODO sendModal?

		class ApplicationCommandStateHolder(private val initial: Received) : ApplicationCommandState, InteractionStateHolder {
			private var state: ApplicationCommandState = initial

			override fun getInitialState(): Received {
				return initial
			}

			override fun getInteraction(): ApplicationCommand {
				return initial.interaction()
			}

			override fun ack(): Thinking {
				val ack = state.ack()
				state = ack
				return ack
			}

			override fun reply(message: InteractionCallbackData.Message): MessageSent {
				val reply = state.reply(message)
				state = reply
				return reply
			}

			override fun edit(message: InteractionCallbackData.Message, messageId: Long?): MessageSent {
				val edit = state.edit(message, messageId)
				state = edit
				return edit
			}
		}


		abstract class Base(private val interaction: ApplicationCommand) : InteractionState(), ApplicationCommandState {
			override fun interaction(): ApplicationCommand {
				return interaction
			}
		}

		class Received internal constructor(
			private val initial: CompletableFuture<InteractionResponse>,
			private val context: ApplicationCommandContext,
		) : Initial, Base(context.interaction) {

			override fun ack(): Thinking {
				return doAck()
			}

			override fun reply(message: InteractionCallbackData.Message): MessageSent {
				return doReply(message)
			}

			fun doAck(): Thinking {
				return checkUsed {
					initial.complete(InteractionResponse.Ack)
					Thinking(context)
				}
			}

			fun doReply(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					initial.complete(InteractionResponse.SendMessage(message))
					MessageSent(context, listOf())
				}
			}
		}

		class Thinking internal constructor(private val context: ApplicationCommandContext) : Base(context.interaction) {

			override fun reply(message: InteractionCallbackData.Message): MessageSent {
				return doReply(message)
			}

			fun doReply(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					context.restService.editOriginalResponse(context.token(), message)
					MessageSent(context, listOf())
				}
			}
		}

		class MessageSent internal constructor(
			private val context: ApplicationCommandContext,
			private val followupMessages: List<Message>,
		) : Base(context.interaction) {

			override fun reply(message: InteractionCallbackData.Message): MessageSent {
				return doReply(message)
			}

			override fun edit(message: InteractionCallbackData.Message, messageId: Long?): MessageSent {
				return doEdit(message, messageId)
			}

			fun doReply(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					val followup = context.restService.createFollowupMessage(context.token(), message)
					MessageSent(context, followupMessages + followup)
				}
			}

			fun doEdit(message: InteractionCallbackData.Message, messageId: Long?): MessageSent {
				return checkUsed {
					if (messageId == null) {
						context.restService.editOriginalResponse(context.token(), message)
						MessageSent(context, followupMessages)
					} else {
						val editedFollowup = context.restService.editFollowupMessage(context.token(), message, messageId)
						MessageSent(context, replaceOrAppend(followupMessages, editedFollowup))
					}
				}
			}
		}
	}

	sealed interface MessageComponentState {

		companion object {
			fun received(interaction: MessageComponent, initial: CompletableFuture<InteractionResponse>, restService: InteractionRestService): Received {
				return Received(initial, MessageComponentContext(restService, interaction))
			}
		}

		fun ack(): Thinking {
			throw IllegalStateException("Cannot ack while in state ${javaClass.simpleName}")
		}

		fun reply(message: InteractionCallbackData.Message): MessageSent {
			throw IllegalStateException("Cannot reply while in state ${javaClass.simpleName}")
		}

		fun edit(message: InteractionCallbackData.Message, messageId: Long? = null): MessageSent {
			throw IllegalStateException("Cannot edit while in state ${javaClass.simpleName}")
		}

		class MessageComponentStateHolder(private val initial: Received) : MessageComponentState, InteractionStateHolder {
			private var state: MessageComponentState = initial

			override fun getInitialState(): Received {
				return initial
			}

			override fun getInteraction(): MessageComponent {
				return initial.interaction()
			}

			override fun ack(): Thinking {
				val ack = state.ack()
				state = ack
				return ack
			}

			override fun reply(message: InteractionCallbackData.Message): MessageSent {
				val reply = state.reply(message)
				state = reply
				return reply
			}

			override fun edit(message: InteractionCallbackData.Message, messageId: Long?): MessageSent {
				val edit = state.edit(message, messageId)
				state = edit
				return edit
			}
		}

		abstract class Base(private val interaction: MessageComponent) : MessageComponentState, InteractionState() {
			override fun interaction(): MessageComponent {
				return interaction
			}

		}

		class Received internal constructor(
			private val initial: CompletableFuture<InteractionResponse>,
			private val context: MessageComponentContext,
		) : Base(context.interaction), Initial {

			override fun ack(): Thinking {
				return doAck()
			}

			override fun reply(message: InteractionCallbackData.Message): MessageSent {
				return doReply(message)
			}


			fun doAck(): Thinking {
				return checkUsed {
					initial.complete(InteractionResponse.AckUpdate)
					Thinking(context)
				}
			}

			fun doReply(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					initial.complete(InteractionResponse.UpdateMessage(message))
					MessageSent(context, listOf())
				}
			}

		}

		class Thinking internal constructor(private val context: MessageComponentContext) : Base(context.interaction) {

			override fun reply(message: InteractionCallbackData.Message): MessageSent {
				return doReply(message)
			}

			fun doReply(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					context.restService.editOriginalResponse(context.token(), message)
					MessageSent(context, listOf())
				}
			}
		}

		class MessageSent internal constructor(
			private val context: MessageComponentContext,
			private val followupMessages: List<Message>,
		) : Base(context.interaction) {

			override fun reply(message: InteractionCallbackData.Message): MessageSent {
				return doReply(message)
			}

			override fun edit(message: InteractionCallbackData.Message, messageId: Long?): MessageSent {
				return doEdit(message, messageId)
			}


			fun doReply(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					val followup = context.restService.createFollowupMessage(context.token(), message)
					MessageSent(context, followupMessages + followup)
				}
			}

			fun doEdit(message: InteractionCallbackData.Message, messageId: Long?): MessageSent {
				return checkUsed {
					if (messageId == null) {
						context.restService.editOriginalResponse(context.token(), message)
						MessageSent(context, followupMessages)
					} else {
						val editedFollowup = context.restService.editFollowupMessage(context.token(), message, messageId)
						MessageSent(context, replaceOrAppend(followupMessages, editedFollowup))
					}
				}
			}
		}
	}

	sealed interface AutocompleteState {

		companion object {
			fun received(interaction: Autocomplete): Received {
				return Received(interaction)
			}
		}

		class AutocompleteStateHolder(private val initial: Received) : AutocompleteState, InteractionStateHolder {

			override fun getInitialState(): Received {
				return initial
			}

			override fun getInteraction(): Autocomplete {
				return initial.interaction()
			}
		}

		abstract class Base(private val interaction: Autocomplete) : AutocompleteState, InteractionState() {
			override fun interaction(): Autocomplete {
				return interaction
			}
		}

		class Received internal constructor(interaction: Autocomplete) : Base(interaction), Initial
	}

	sealed interface ModalState {
		companion object {
			fun received(interaction: ModalSubmit): Received {
				return Received(interaction)
			}
		}

		class ModalStateHolder(private val initial: Received) : AutocompleteState, InteractionStateHolder {

			override fun getInitialState(): Received {
				return initial
			}

			override fun getInteraction(): ModalSubmit {
				return initial.interaction()
			}
		}

		abstract class Base(private val interaction: ModalSubmit) : InteractionState(), ModalState {

			override fun interaction(): ModalSubmit {
				return interaction
			}
		}

		class Received internal constructor(interaction: ModalSubmit) : Base(interaction), Initial


	}

}
