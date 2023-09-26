package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.InteractionStateHolder
import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallback.FollowupMessage
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallback.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.ApplicationCommand
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.MessageFlag.EPHEMERAL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandConfiguration {

	private val debugGuildId: Long = 214539058028740609L

	@Bean
	fun henloCommand(): InteractionCommand {
		return object : InteractionCommand {
			override fun creation(): CreateCommand {
				return CreateCommand.CreateUserGuildCommand("henlo", listOf(debugGuildId))
			}

			override suspend fun onInteraction(stateHolder: InteractionStateHolder) {
				when (val interaction = stateHolder.interaction) {
					is ApplicationCommand -> stateHolder.completeOrEditOriginal(Message("Henlo, ${interaction.data.resolved?.users?.values?.first()?.username}!")).await()
					else -> throw IllegalStateException("Unhandled interaction type ${interaction.type}")
				}
			}
		}
	}

	@Bean
	fun testCommand(): InteractionCommand {
		return object : InteractionCommand {
			override fun creation(): CreateCommand {
				return CreateCommand.CreateSlashGuildCommand("test", "Test command", listOf(debugGuildId))
			}

			override suspend fun onInteraction(stateHolder: InteractionStateHolder) {

				stateHolder.ack(false).await()

				stateHolder.completeOrEditOriginal(Message("Public")).await()

				val id = stateHolder.followup(FollowupMessage(Message("Private!", flags = IntBitfield(listOf(EPHEMERAL))))).await().message.id

				stateHolder.completeOrFollowup(Message("Public2")).await()

				stateHolder.editFollowup(id, FollowupMessage(Message("Private2"))).await()
			}
		}

	}
}
