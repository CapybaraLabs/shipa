package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import dev.capybaralabs.shipa.discord.interaction.validation.SaltyCoffeeInteractionValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class InteractionsConfiguration(private val discord: DiscordProperties) {

	@Bean
	fun interactionValidator(): InteractionValidator {
		return SaltyCoffeeInteractionValidator(discord.publicKey)
	}

	@Bean
	@OptIn(ExperimentalCoroutinesApi::class)
	internal fun interactionCoroutineScope(): CoroutineScope {
		return CoroutineScope(Dispatchers.IO.limitedParallelism(100))
	}

}
