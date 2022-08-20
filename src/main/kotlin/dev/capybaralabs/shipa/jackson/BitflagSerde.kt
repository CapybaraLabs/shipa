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
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.IntBitflag
import dev.capybaralabs.shipa.discord.model.StringBitfield
import dev.capybaralabs.shipa.discord.model.StringBitflag
import java.math.BigInteger

class IntBitfieldSerializer : JsonSerializer<IntBitfield<*>>() {

	override fun handledType(): Class<IntBitfield<*>> {
		return IntBitfield::class.java
	}

	override fun serialize(value: IntBitfield<*>, gen: JsonGenerator, serializers: SerializerProvider) {
		val reduced: Int = value.map { it.value }.reduceOrNull { acc, i -> acc or i } ?: 0
		gen.writeNumber(reduced)
	}
}

class IntBitfieldDeserializer(private val type: JavaType) : JsonDeserializer<IntBitfield<*>>(), ContextualDeserializer {

	override fun handledType(): Class<IntBitfield<*>> {
		return IntBitfield::class.java
	}

	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntBitfield<*> {
		val flags = ctxt.readValue(p, Int::class.java)

		return type.rawClass.enumConstants.asList()
			.map { it as IntBitflag }
			.filter { flags and it.value == it.value }
			.let { IntBitfield(it) }
	}

	override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
		return IntBitfieldDeserializer(property.type.containedType(0))
	}

}


class StringBitfieldSerializer : JsonSerializer<StringBitfield<*>>() {

	override fun handledType(): Class<StringBitfield<*>> {
		return StringBitfield::class.java
	}

	override fun serialize(value: StringBitfield<*>, gen: JsonGenerator, serializers: SerializerProvider) {
		val reduced: BigInteger = value.map { it.value.toBigInteger() }.reduceOrNull { acc, i -> acc.or(i) } ?: BigInteger.ZERO
		gen.writeString(reduced.toString())
	}
}

class StringBitfieldDeserializer(private val type: JavaType) : JsonDeserializer<StringBitfield<*>>(), ContextualDeserializer {

	override fun handledType(): Class<StringBitfield<*>> {
		return StringBitfield::class.java
	}

	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StringBitfield<*> {
		val flags = ctxt.readValue(p, String::class.java).toBigInteger()

		return type.rawClass.enumConstants.asList()
			.map { it as StringBitflag }
			.filter {
				val value = it.value.toBigInteger()
				flags.and(value) == value
			}
			.let { StringBitfield(it) }
	}

	override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
		return StringBitfieldDeserializer(property.type.containedType(0))
	}

}
