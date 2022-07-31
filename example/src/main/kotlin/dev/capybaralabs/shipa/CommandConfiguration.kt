package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData.Message
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage
import dev.capybaralabs.shipa.discord.interaction.model.create.Command
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateUserCommand
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandConfiguration {

	@Bean
	fun henloCommand(): Command {
		return Command.Stub(
			CreateUserCommand("henlo"),
			onApplicationCommand = { SendMessage(Message(content = "Henlo, ${it.data.resolved?.users?.values?.first()?.username}!")) }
		)
	}
}
