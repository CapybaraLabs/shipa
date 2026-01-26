package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.jackson.ShipaJsonMapper
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter


@Configuration
class TestConfiguration(
	private val shipaJsonMapper: ShipaJsonMapper,
) {

	@Bean
	fun builder(): RestTemplateBuilder {
		val converter = JacksonJsonHttpMessageConverter(shipaJsonMapper.mapper)
		return RestTemplateBuilder().additionalMessageConverters(converter)
	}

}
