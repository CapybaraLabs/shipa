package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackDataMessage
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.CHANNEL_MESSAGE_WITH_SOURCE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.create.Command
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateUserCommand
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandConfiguration {

	@Bean
	fun henloCommand(): Command {
		return Command(
			CreateUserCommand("henlo")
		) { InteractionResponse(CHANNEL_MESSAGE_WITH_SOURCE, InteractionCallbackDataMessage(content = "Henlo, ${(it.data as ApplicationCommandData).resolved?.users?.values?.first()?.username}!")) }
	}
}
