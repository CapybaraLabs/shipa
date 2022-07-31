package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import dev.capybaralabs.shipa.discord.interaction.validation.SaltyCoffeeInteractionValidator
import dev.capybaralabs.shipa.logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate


@Configuration
class InteractionsConfiguration(private val discord: DiscordProperties) {

	@Bean
	fun interactionValidator(): InteractionValidator {
		return SaltyCoffeeInteractionValidator(discord.publicKey)
	}

	@Bean
	@OptIn(ExperimentalCoroutinesApi::class)
	fun interactionCoroutineScope(): CoroutineScope {
		return CoroutineScope(Dispatchers.IO.limitedParallelism(100))
	}

	@Bean
	fun restTemplate(properties: DiscordProperties, @Suppress("SpringJavaInjectionPointsAutowiringInspection") converter: MappingJackson2HttpMessageConverter): RestTemplate {
		var builder = RestTemplateBuilder()
			.rootUri("https://discord.com/api/v10/")
			.messageConverters(converter)
			.additionalInterceptors(
				ClientHttpRequestInterceptor { req, body, exec ->
					req.headers.add("Authorization", "Bot " + properties.botToken)
					exec.execute(req, body)
				}
			)

		if (logger().isDebugEnabled) {
			builder = builder
				.requestFactory { BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory()) }
				.additionalInterceptors(LoggingInterceptor())
		}

		return builder.build()
	}

	/**
	 * https://www.baeldung.com/spring-resttemplate-logging
	 */
	private class LoggingInterceptor : ClientHttpRequestInterceptor {

		override fun intercept(req: HttpRequest, reqBody: ByteArray, ex: ClientHttpRequestExecution): ClientHttpResponse {
			logger().debug("Request body: {}", String(reqBody, StandardCharsets.UTF_8))
			val response: ClientHttpResponse = ex.execute(req, reqBody)

			val reader = InputStreamReader(response.body, StandardCharsets.UTF_8)
			val body = BufferedReader(reader).lines()
				.collect(Collectors.joining("\n"))

			logger().debug("Response body: {}", body)
			return response
		}

	}
}
