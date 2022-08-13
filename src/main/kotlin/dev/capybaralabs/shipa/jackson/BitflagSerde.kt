package dev.capybaralabs.shipa.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import dev.capybaralabs.shipa.discord.model.Bitfield
import dev.capybaralabs.shipa.discord.model.Bitflag

class BitfieldSerializer : JsonSerializer<Bitfield<*>>() {

	override fun handledType(): Class<Bitfield<*>> {
		return Bitfield::class.java
	}

	override fun serialize(value: Bitfield<*>, gen: JsonGenerator, serializers: SerializerProvider) {
		val reduced: Int = value.map { it.value }.reduceOrNull { acc, i -> acc or i } ?: 0
		gen.writeNumber(reduced)
	}
}

class BitfieldDeserializer(private val type: JavaType) : JsonDeserializer<Bitfield<*>>(), ContextualDeserializer {

	override fun handledType(): Class<Bitfield<*>> {
		return Bitfield::class.java
	}

	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Bitfield<*> {
		val flags = ctxt.readValue(p, Int::class.java)

		return type.rawClass.enumConstants.asList()
			.map { it as Bitflag }
			.filter { flags and it.value == it.value }
			.let { Bitfield(it) }
	}

	override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
		return BitfieldDeserializer(property.type.containedType(0))
	}

}
