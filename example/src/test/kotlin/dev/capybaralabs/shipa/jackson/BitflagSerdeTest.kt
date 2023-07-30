package dev.capybaralabs.shipa.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.type.SimpleType
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.IntBitflag
import dev.capybaralabs.shipa.discord.model.MessageFlag
import dev.capybaralabs.shipa.discord.model.MessageFlag.CROSSPOSTED
import dev.capybaralabs.shipa.discord.model.MessageFlag.EPHEMERAL
import dev.capybaralabs.shipa.discord.model.MessageFlag.FAILED_TO_MENTION_SOME_ROLES_IN_THREAD
import dev.capybaralabs.shipa.discord.model.Permission
import dev.capybaralabs.shipa.discord.model.Permission.ADMINISTRATOR
import dev.capybaralabs.shipa.discord.model.Permission.CREATE_INSTANT_INVITE
import dev.capybaralabs.shipa.discord.model.Permission.MODERATE_MEMBERS
import dev.capybaralabs.shipa.discord.model.StringBitfield
import dev.capybaralabs.shipa.discord.model.StringBitflag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.verify

internal class BitflagSerdeTest {

	@Test
	internal fun intBitfield() {
		assertSerdeIntBitfield(listOf(CROSSPOSTED), "1".toInt(2))
		assertSerdeIntBitfield(listOf(EPHEMERAL), "1000000".toInt(2))
		assertSerdeIntBitfield(listOf(FAILED_TO_MENTION_SOME_ROLES_IN_THREAD), "100000000".toInt(2))

		val otherFlags = MessageFlag.entries.filterNot { listOf(CROSSPOSTED, EPHEMERAL, FAILED_TO_MENTION_SOME_ROLES_IN_THREAD).contains(it) }
		assertSerdeIntBitfield(otherFlags, "10111110".toInt(2))
	}

	private fun assertSerdeIntBitfield(deserialized: List<MessageFlag>, serialized: Int) {
		assertSerializeIntBitfield(deserialized, serialized)
		assertDeserializeIntBitfield(serialized, deserialized)
	}

	private fun assertSerializeIntBitfield(input: List<IntBitflag>, expected: Int) {
		val bitField = IntBitfield(input)
		val serializer = IntBitfieldSerializer()

		val gen = mock(JsonGenerator::class.java)
		val captor = ArgumentCaptor.forClass(Int::class.java)


		serializer.serialize(bitField, gen, mock(SerializerProvider::class.java))

		verify(gen).writeNumber(captor.capture())

		assertThat(captor.value).isEqualTo(expected)
	}

	private fun assertDeserializeIntBitfield(input: Int, expected: List<MessageFlag>) {
		val deserializer = IntBitfieldDeserializer(SimpleType.constructUnsafe(MessageFlag::class.java))

		val ctxt = mock(DeserializationContext::class.java)
		`when`(ctxt.readValue(isA(), eq(Int::class.java))).thenReturn(input)

		val result = deserializer.deserialize(mock(JsonParser::class.java), ctxt)

		assertThat(result).containsExactlyInAnyOrderElementsOf(expected)
	}


	@Test
	internal fun serializeStringBitfield() {
		assertSerdeStringBitfield(listOf(CREATE_INSTANT_INVITE), "1".toBigInteger(2).toString())
		assertSerdeStringBitfield(listOf(ADMINISTRATOR), "1000".toBigInteger(2).toString())
		assertSerdeStringBitfield(listOf(MODERATE_MEMBERS), "10000000000000000000000000000000000000000".toBigInteger(2).toString())

		val otherPerms = Permission.entries.filterNot { listOf(CREATE_INSTANT_INVITE, ADMINISTRATOR, MODERATE_MEMBERS).contains(it) }
		assertSerdeStringBitfield(otherPerms, "1111111111111111111111111111111111110110".toBigInteger(2).toString())
	}

	private fun assertSerdeStringBitfield(deserialized: List<Permission>, serialized: String) {
		assertSerializeStringBitfield(deserialized, serialized)
		assertDeserializeStringBitfield(serialized, deserialized)
	}

	private fun assertSerializeStringBitfield(input: List<StringBitflag>, expected: String) {
		val bitfield = StringBitfield(input)
		val serializer = StringBitfieldSerializer()

		val gen = mock(JsonGenerator::class.java)
		val captor = ArgumentCaptor.forClass(String::class.java)


		serializer.serialize(bitfield, gen, mock(SerializerProvider::class.java))

		verify(gen).writeString(captor.capture())

		assertThat(captor.value).isEqualTo(expected)
	}

	private fun assertDeserializeStringBitfield(input: String, expected: List<Permission>) {
		val deserializer = StringBitfieldDeserializer(SimpleType.constructUnsafe(Permission::class.java))

		val ctxt = mock(DeserializationContext::class.java)
		`when`(ctxt.readValue(isA(), eq(String::class.java))).thenReturn(input)

		val result = deserializer.deserialize(mock(JsonParser::class.java), ctxt)

		assertThat(result).containsExactlyInAnyOrderElementsOf(expected)
	}
}
