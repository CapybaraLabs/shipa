package dev.capybaralabs.shipa.discord.client

import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.ratelimit.BucketService
import dev.capybaralabs.shipa.jackson.ShipaJsonMapper
import dev.capybaralabs.shipa.logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter

@Configuration
class DiscordClientConfiguration(
	private val properties: DiscordProperties,
	private val shipaJsonMapper: ShipaJsonMapper,
	private val bucketService: BucketService,
	private val metrics: ShipaMetrics,
	private val restTemplateBuilder: RestTemplateBuilder,
) {

	private val libUrl = "https://github.com/CapybaraLabs/shipa"
	private val libVersion = this.javaClass.`package`?.implementationVersion ?: "development"
	private val userAgent = "DiscordBot ($libUrl, $libVersion)" // https://discord.com/developers/docs/reference#user-agent

	@Bean
	fun discordRestService(): DiscordRestService {
		return DiscordRestService(
			DiscordAuthToken.Bot(properties.botToken),
			restTemplateBuilder(),
			bucketService,
			metrics,
		)
	}

	private fun restTemplateBuilder(): RestTemplateBuilder {
		val converter = JacksonJsonHttpMessageConverter(shipaJsonMapper.mapper)
		val formConverter = FormHttpMessageConverter()
		formConverter.addPartConverter(converter)
		var builder = restTemplateBuilder
			.rootUri(properties.discordApiRootUrl)
			.messageConverters(converter, formConverter)
			.additionalInterceptors(
				{ req, body, exec ->
					req.headers.add(HttpHeaders.USER_AGENT, userAgent)
					exec.execute(req, body)
				},
			)

		val requestFactory = builder.buildRequestFactory()
		if (requestFactory == null) {
			val msg = "Please include an Http Client implementation, e.g. Apache HttpComponents or OkHttp3"
			val e = RuntimeException(msg)
			logger().error(msg, e)
			throw e
		}
		if (requestFactory is SimpleClientHttpRequestFactory) {
			logger().warn("Please include either Apache HttpComponents4 or OkHttp3 http client lib in your class path. The simple client based on Java's URL does not work properly with PATCH requests, and doesn't handle 429s Ratelimits gracefully.")
		}

		if (logger().isDebugEnabled) {
			builder = builder
				.requestFactoryBuilder { _ -> BufferingClientHttpRequestFactory(requestFactory) }
				.additionalInterceptors(LoggingInterceptor())
		}

		return builder
	}

	/**
	 * https://www.baeldung.com/spring-resttemplate-logging
	 */
	private class LoggingInterceptor : ClientHttpRequestInterceptor {

		override fun intercept(req: HttpRequest, reqBody: ByteArray, ex: ClientHttpRequestExecution): ClientHttpResponse {
			logger().debug("Request headers: {} body: {}", req.headers, String(reqBody, StandardCharsets.UTF_8))
			val response: ClientHttpResponse = ex.execute(req, reqBody)

			val reader = InputStreamReader(response.body, StandardCharsets.UTF_8)
			val body = BufferedReader(reader).lines()
				.collect(Collectors.joining("\n"))

			logger().debug("Response headers: {} body: {}", response.headers, body)
			return response
		}

	}
}
