package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.interaction.validation.InstrumentedInteractionValidator
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import dev.capybaralabs.shipa.discord.interaction.validation.SaltyCoffeeInteractionValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class InteractionsConfiguration(private val discord: DiscordProperties) {

	@Bean
	fun interactionValidator(shipaMetrics: ShipaMetrics): InteractionValidator {
		return InstrumentedInteractionValidator("salty-coffee", SaltyCoffeeInteractionValidator(discord.publicKey), shipaMetrics)
	}

	@Bean
	@OptIn(ExperimentalCoroutinesApi::class)
	internal fun interactionCoroutineScope(): CoroutineScope {
		val supervisor = SupervisorJob()
		return CoroutineScope(Dispatchers.IO.limitedParallelism(100) + supervisor)
	}

}
