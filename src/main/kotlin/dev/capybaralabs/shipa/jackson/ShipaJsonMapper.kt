package dev.capybaralabs.shipa.jackson

import com.fasterxml.jackson.annotation.JsonInclude.Include
import org.springframework.stereotype.Component
import tools.jackson.core.StreamReadFeature
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.type.SimpleType
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

/**
 * Holds the [JsonMapper] Jackson configuration used by Shipa
 */
@Component
class ShipaJsonMapper {

	val mapper: JsonMapper = jsonMapper {
		changeDefaultPropertyInclusion {
			it
				.withValueInclusion(Include.NON_NULL)
				.withContentInclusion(Include.NON_NULL)
		}
		propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
		disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
		disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS) // write dates as ISO strings
		enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION) // improve exception messages

		val intBitfieldDeserializer = IntBitfieldDeserializer(SimpleType.constructUnsafe(Void::class.java))
		val stringBitfieldDeserializer = StringBitfieldDeserializer(SimpleType.constructUnsafe(Void::class.java))

		val shipaModule = SimpleModule()
			.addSerializer(IntBitfieldSerializer())
			.addSerializer(StringBitfieldSerializer())
			.addDeserializer(intBitfieldDeserializer.handledType(), intBitfieldDeserializer)
			.addDeserializer(stringBitfieldDeserializer.handledType(), stringBitfieldDeserializer)

		addModule(shipaModule)
		addModule(kotlinModule())
	}
}
