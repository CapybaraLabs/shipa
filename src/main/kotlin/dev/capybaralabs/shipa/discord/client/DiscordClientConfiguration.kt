package dev.capybaralabs.shipa.discord.client

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
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
class DiscordClientConfiguration {

	@Bean
	fun restTemplate(properties: DiscordProperties, converter: MappingJackson2HttpMessageConverter): RestTemplate {
		var builder = RestTemplateBuilder()
			.rootUri(properties.discordApiRootUrl)
			.messageConverters(converter)
			.additionalInterceptors(
				ClientHttpRequestInterceptor { req, body, exec ->
					req.headers.add("Authorization", "Bot " + properties.botToken)
					exec.execute(req, body)
				}
			)

		val requestFactory = builder.buildRequestFactory()
		if (requestFactory is SimpleClientHttpRequestFactory) {
			logger().warn("Please include either Apache HttpComponents4 or OkHttp3 http client lib in your class path. The simple client based on Java's URL does not work properly with PATCH requests, and doesn't handle 429s Ratelimits gracefully.")
		}

		if (logger().isDebugEnabled) {
			builder = builder
				.requestFactory { _ -> BufferingClientHttpRequestFactory(requestFactory) }
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
