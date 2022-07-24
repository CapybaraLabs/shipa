package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
class InteractionsConfiguration(private val discord: DiscordProperties) {

	@Bean
	fun interactionValidator(): InteractionValidator {
		return SaltyCoffeeInteractionValidator(discord.publicKey)
	}

	@Bean
	fun restTemplate(properties: DiscordProperties): RestTemplate {
		return RestTemplateBuilder()
			.rootUri("https://discord.com/api/v10/")
			.additionalInterceptors(
				ClientHttpRequestInterceptor { req, body, exec ->
					req.headers.add("Authorization", "Bot " + properties.botToken)
					exec.execute(req, body)
				}
			)
			.build()
	}
}
