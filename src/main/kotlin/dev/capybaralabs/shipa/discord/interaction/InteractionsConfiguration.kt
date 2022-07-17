package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InteractionsConfiguration(private val discord: DiscordProperties) {

	@Bean
	fun interactionValidator(): InteractionValidator {
		return SaltyCoffeeInteractionValidator(discord.publicKey)
	}
}
