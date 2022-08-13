package dev.capybaralabs.shipa.jackson

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.type.SimpleType
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

			it.serializers(BitfieldSerializer())
			it.deserializers(BitfieldDeserializer(SimpleType.constructUnsafe(Void::class.java)))
		}
	}
}
