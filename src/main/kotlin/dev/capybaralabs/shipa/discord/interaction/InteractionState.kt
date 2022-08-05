package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.InteractionState.MessageComponentState.MessageSent
import dev.capybaralabs.shipa.discord.interaction.InteractionState.MessageComponentState.Received
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
			throw IllegalStateException("This state has been used, continue with the return value!")
		}
	}


	sealed class ApplicationCommandState(private val context: ApplicationCommandContext) : InteractionState() {

		companion object {
			fun received(interaction: ApplicationCommand, initial: CompletableFuture<InteractionResponse>, restService: InteractionRestService): Received {
				return Received(initial, ApplicationCommandContext(restService, interaction))
			}
		}

		override fun interaction(): ApplicationCommand {
			return context.interaction
		}


		class Received internal constructor(private val initial: CompletableFuture<InteractionResponse>, private val context: ApplicationCommandContext) : ApplicationCommandState(context) {

			fun ack(): Thinking {
				return checkUsed {
					initial.complete(InteractionResponse.Ack)
					Thinking(context)
				}
			}

			fun reply(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					initial.complete(InteractionResponse.SendMessage(message))
					MessageSent(context, listOf())
				}
			}

			// TODO sendModal?

		}

		class Thinking internal constructor(private val context: ApplicationCommandContext) : ApplicationCommandState(context) {

			fun editOriginal(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					context.restService.editOriginalResponse(context.token(), message)
					MessageSent(context, listOf())
				}
			}
		}

		class MessageSent internal constructor(private val context: ApplicationCommandContext, private val followupMessages: List<Message>) : ApplicationCommandState(context) {

			fun editOriginal(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					context.restService.editOriginalResponse(context.token(), message)
					MessageSent(context, followupMessages)
				}
			}

			fun createFollowup(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					val followup = context.restService.createFollowupMessage(context.token(), message)
					MessageSent(context, followupMessages + followup)
				}
			}

			fun editFollowup(messageId: Long, message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					val editedFollowup = context.restService.editFollowupMessage(context.token(), message, messageId)
					MessageSent(context, replaceOrAppend(followupMessages, editedFollowup))
				}
			}

			private fun replaceOrAppend(messages: List<Message>, message: Message): List<Message> {
				val index = followupMessages.indexOfFirst { it.id == message.id }
				return if (index < 0) {
					messages + message
				} else {
					messages.mapIndexed { i, it -> if (i == index) message else it }
				}
			}
		}
	}


	sealed class MessageComponentState(private val context: MessageComponentContext) : InteractionState() {

		companion object {
			fun received(interaction: MessageComponent, initial: CompletableFuture<InteractionResponse>, restService: InteractionRestService): Received {
				return Received(initial, MessageComponentContext(restService, interaction))
			}
		}

		override fun interaction(): MessageComponent {
			return context.interaction
		}

		class Received internal constructor(private val initial: CompletableFuture<InteractionResponse>, private val context: MessageComponentContext) : MessageComponentState(context) {

			fun ack(): Thinking {
				return checkUsed {
					initial.complete(InteractionResponse.AckUpdate)
					Thinking(context)
				}
			}

			fun reply(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					initial.complete(InteractionResponse.UpdateMessage(message))
					MessageSent(context, listOf())
				}
			}

		}

		class Thinking internal constructor(private val context: MessageComponentContext) : MessageComponentState(context) {

			fun editOriginal(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					context.restService.editOriginalResponse(context.token(), message)
					MessageSent(context, listOf())
				}
			}
		}

		class MessageSent internal constructor(private val context: MessageComponentContext, private val followupMessages: List<Message>) : MessageComponentState(context) {

			fun editOriginal(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					context.restService.editOriginalResponse(context.token(), message)
					MessageSent(context, followupMessages)
				}
			}

			fun createFollowup(message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					val followup = context.restService.createFollowupMessage(context.token(), message)
					MessageSent(context, followupMessages + followup)
				}
			}

			fun editFollowup(messageId: Long, message: InteractionCallbackData.Message): MessageSent {
				return checkUsed {
					val editedFollowup = context.restService.editFollowupMessage(context.token(), message, messageId)
					MessageSent(context, replaceOrAppend(followupMessages, editedFollowup))
				}
			}

			private fun replaceOrAppend(messages: List<Message>, message: Message): List<Message> {
				val index = followupMessages.indexOfFirst { it.id == message.id }
				return if (index < 0) {
					messages + message
				} else {
					messages.mapIndexed { i, it -> if (i == index) message else it }
				}
			}
		}
	}


	sealed class AutocompleteState(private val interaction: Autocomplete) : InteractionState() {

		companion object {
			fun received(interaction: Autocomplete): Received {
				return Received(interaction)
			}
		}

		override fun interaction(): Autocomplete {
			return interaction
		}

		class Received internal constructor(interaction: Autocomplete) : AutocompleteState(interaction)
	}

	sealed class ModalState(private val interaction: ModalSubmit) : InteractionState() {

		companion object {
			fun received(interaction: ModalSubmit): Received {
				return Received(interaction)
			}
		}

		override fun interaction(): ModalSubmit {
			return interaction
		}

		class Received internal constructor(interaction: ModalSubmit) : ModalState(interaction)


	}

}
