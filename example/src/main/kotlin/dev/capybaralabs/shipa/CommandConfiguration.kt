package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.InteractionState.ApplicationCommandState.ApplicationCommandStateHolder
import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandConfiguration {

	private val debugGuildId: Long = 214539058028740609L

	@Bean
	fun henloCommand(): InteractionCommand {
		return object : InteractionCommand {
			override fun creation(): CreateCommand {
				return CreateCommand.User("henlo", debugGuildId)
			}

			override fun onApplicationCommand(stateHolder: ApplicationCommandStateHolder) {
				stateHolder.reply(Message(content = "Henlo, ${stateHolder.getInteraction().data.resolved?.users?.values?.first()?.username}!"))
			}
		}
	}
}
