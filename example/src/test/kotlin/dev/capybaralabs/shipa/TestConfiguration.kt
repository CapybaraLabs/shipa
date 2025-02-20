package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.jackson.ShipaJsonMapper
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter


@Configuration
class TestConfiguration(
	private val shipaJsonMapper: ShipaJsonMapper,
) {

	@Bean
	fun builder(): RestTemplateBuilder {
		val converter = MappingJackson2HttpMessageConverter(shipaJsonMapper.mapper)
		return RestTemplateBuilder().additionalMessageConverters(converter)
	}

}
