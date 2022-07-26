package dev.capybaralabs.shipa

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {

	@Bean
	fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
		return Jackson2ObjectMapperBuilderCustomizer {
			it.serializationInclusion(Include.NON_NULL)
			it.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
		}
	}
}
