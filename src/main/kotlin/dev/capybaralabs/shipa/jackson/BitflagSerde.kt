package dev.capybaralabs.shipa.jackson

import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.IntBitflag
import dev.capybaralabs.shipa.discord.model.StringBitfield
import dev.capybaralabs.shipa.discord.model.StringBitflag
import java.math.BigInteger
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer

class IntBitfieldSerializer : ValueSerializer<IntBitfield<*>>() {

	override fun handledType(): Class<IntBitfield<*>> {
		return IntBitfield::class.java
	}

	override fun serialize(value: IntBitfield<*>, gen: JsonGenerator, serializers: SerializationContext) {
		val reduced: Int = value.map { it.value }.reduceOrNull { acc, i -> acc or i } ?: 0
		gen.writeNumber(reduced)
	}
}

class IntBitfieldDeserializer(private val type: JavaType) : ValueDeserializer<IntBitfield<*>>() {

	override fun handledType(): Class<IntBitfield<*>> {
		return IntBitfield::class.java
	}

	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntBitfield<*> {
		val flags = ctxt.readValue(p, Int::class.java)

		val enumConstants = type.rawClass.enumConstants ?: throw IllegalArgumentException("${type.rawClass} needs to be an enum!")

		return enumConstants.asList()
			.map { it as IntBitflag }
			.filter { flags and it.value == it.value }
			.let { IntBitfield(it) }
	}

	override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): ValueDeserializer<*> {
		var containedType = property.type.containedType(0)
		if (containedType.rawClass.isAssignableFrom(IntBitfield::class.java)) { // Optional<IntBitfield<*>> can trigger this
			containedType = containedType.containedType(0)
		}

		return IntBitfieldDeserializer(containedType)
	}

}


class StringBitfieldSerializer : ValueSerializer<StringBitfield<*>>() {

	override fun handledType(): Class<StringBitfield<*>> {
		return StringBitfield::class.java
	}

	override fun serialize(value: StringBitfield<*>, gen: JsonGenerator, serializers: SerializationContext) {
		val reduced: BigInteger = value.map { it.value.toBigInteger() }.reduceOrNull { acc, i -> acc.or(i) } ?: BigInteger.ZERO
		gen.writeString(reduced.toString())
	}
}

class StringBitfieldDeserializer(private val type: JavaType) : ValueDeserializer<StringBitfield<*>>() {

	override fun handledType(): Class<StringBitfield<*>> {
		return StringBitfield::class.java
	}

	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StringBitfield<*> {
		val flags = ctxt.readValue(p, String::class.java).toBigInteger()

		val enumConstants = type.rawClass.enumConstants ?: throw IllegalArgumentException("${type.rawClass} needs to be an enum!")

		return enumConstants.asList()
			.map { it as StringBitflag }
			.filter {
				val value = it.value.toBigInteger()
				flags.and(value) == value
			}
			.let { StringBitfield(it) }
	}

	override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): ValueDeserializer<*> {
		var containedType = property.type.containedType(0)
		if (containedType.rawClass.isAssignableFrom(StringBitfield::class.java)) { // Optional<StringBitfield<*>> can trigger this
			containedType = containedType.containedType(0)
		}

		return StringBitfieldDeserializer(containedType)
	}

}
