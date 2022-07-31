package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.ApplicationCommand
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage
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

			override fun onApplicationCommand(interaction: ApplicationCommand): Sequence<InteractionResponse> {
				return sequenceOf(SendMessage(Message(content = "Henlo, ${interaction.data.resolved?.users?.values?.first()?.username}!")))
			}
		}
	}
}
