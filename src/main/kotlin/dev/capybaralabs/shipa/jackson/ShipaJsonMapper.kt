package dev.capybaralabs.shipa.jackson

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.SimpleType
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.stereotype.Component

/**
 * Holds the [JsonMapper] Jackson configuration used by Shipa
 */
@Component
class ShipaJsonMapper {

	val mapper: JsonMapper = jsonMapper {
		serializationInclusion(Include.NON_NULL)
		propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
		disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
		disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // write dates as ISO strings

		val intBitfieldDeserializer = IntBitfieldDeserializer(SimpleType.constructUnsafe(Void::class.java))
		val stringBitfieldDeserializer = StringBitfieldDeserializer(SimpleType.constructUnsafe(Void::class.java))

		val shipaModule = SimpleModule()
			.addSerializer(IntBitfieldSerializer())
			.addSerializer(StringBitfieldSerializer())
			.addDeserializer(intBitfieldDeserializer.handledType(), intBitfieldDeserializer)
			.addDeserializer(stringBitfieldDeserializer.handledType(), stringBitfieldDeserializer)

		addModule(shipaModule)
		addModule(Jdk8Module())
		addModule(JavaTimeModule())
		addModule(kotlinModule())
		// not sure if necessary, but it was originally active, probably. findAndRegisterModules() finds it
		addModule(ParameterNamesModule())
	}
}
