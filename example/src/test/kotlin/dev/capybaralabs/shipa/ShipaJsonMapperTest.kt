package dev.capybaralabs.shipa

import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.MessageFlag
import dev.capybaralabs.shipa.discord.model.MessageFlag.CROSSPOSTED
import dev.capybaralabs.shipa.discord.model.MessageFlag.EPHEMERAL
import dev.capybaralabs.shipa.discord.model.MessageFlag.FAILED_TO_MENTION_SOME_ROLES_IN_THREAD
import dev.capybaralabs.shipa.discord.model.MessageFlag.URGENT
import dev.capybaralabs.shipa.discord.model.Permission
import dev.capybaralabs.shipa.discord.model.Permission.ADMINISTRATOR
import dev.capybaralabs.shipa.discord.model.Permission.CREATE_INSTANT_INVITE
import dev.capybaralabs.shipa.discord.model.Permission.MODERATE_MEMBERS
import dev.capybaralabs.shipa.discord.model.StringBitfield
import dev.capybaralabs.shipa.jackson.ShipaJsonMapper
import java.util.Optional
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.json.JsonMapper
import org.assertj.core.api.Assertions.assertThat as assertJThat

/**
 * Making extensive use of [json-path-assert](https://github.com/json-path/JsonPath/tree/master/json-path-assert) here.
 */
internal class ShipaJsonMapperTest : ApplicationTest() {

	@Autowired
	private lateinit var shipaJsonMapper: ShipaJsonMapper

	private val mapper: JsonMapper by lazy { shipaJsonMapper.mapper }

	data class TestObject(
		val regular: String = "foo",
		val optional: String? = null,
		val nullable: Optional<String> = Optional.empty(),
		val optionalNullable: Optional<String>? = null,
		val snakeCase: String = "hssssss",
	)

	@Test
	internal fun baseline() {
		val payload = TestObject()
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.regular", equalTo("foo")))
		assertThat(json, hasNoJsonPath("$.optional"))
		assertThat(json, hasJsonPath("$.nullable", nullValue()))
		assertThat(json, hasNoJsonPath("$.optional_nullable"))

		val value = mapper.readValue(json, TestObject::class.java)

		assertJThat(value.regular).isEqualTo("foo")
		assertJThat(value.optional).isNull()
		assertJThat(value.nullable).isEmpty
		assertJThat(value.optionalNullable).isNull()
	}


	@Test
	internal fun givenOptionalFieldIsNull_whenSerialized_isMissing() {
		val payload = TestObject(optional = null)
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasNoJsonPath("$.optional"))

		val value = mapper.readValue(json, TestObject::class.java)
		assertJThat(value.optional).isNull()
	}

	@Test
	internal fun givenOptionalFieldHasValue_whenSerialized_isPresentWithValue() {
		val payload = TestObject(optional = "bar")
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.optional", equalTo("bar")))

		val value = mapper.readValue(json, TestObject::class.java)
		assertJThat(value.optional).isEqualTo("bar")
	}


	@Test
	internal fun givenNullableFieldIsEmpty_whenSerialized_isPresentWithNull() {
		val payload = TestObject(nullable = Optional.empty())
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.nullable", nullValue()))

		val value = mapper.readValue(json, TestObject::class.java)
		assertJThat(value.nullable).isEmpty
	}

	@Test
	internal fun givenNullableFieldHasValue_whenSerialized_isPresentWithValue() {
		val payload = TestObject(nullable = Optional.of("bar"))
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.nullable", equalTo("bar")))

		val value = mapper.readValue(json, TestObject::class.java)
		assertJThat(value.nullable).hasValue("bar")
	}


	@Test
	internal fun givenOptionalNullableFieldIsNull_whenSerialized_isMissing() {
		val payload = TestObject(optionalNullable = null)
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasNoJsonPath("$.optional_nullable"))

		val value = mapper.readValue(json, TestObject::class.java)
		assertJThat(value.optionalNullable).isNull()
	}

	@Test
	internal fun givenOptionalNullableFieldIsEmpty_whenSerialized_isPresentWithNull() {
		val payload = TestObject(optionalNullable = Optional.empty())
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.optional_nullable", nullValue()))

		val value = mapper.readValue(json, TestObject::class.java)
		assertJThat(value.optionalNullable).isEmpty
	}

	@Test
	internal fun givenOptionalNullableFieldHasValue_whenSerialized_isPresentWithValue() {
		val payload = TestObject(optionalNullable = Optional.of("bar"))
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.optional_nullable", equalTo("bar")))

		val value = mapper.readValue(json, TestObject::class.java)
		assertJThat(value.optionalNullable).hasValue("bar")
	}

	@Test
	internal fun givenCamelCaseProperty_whenSerialized_writeSnakeCase() {
		val payload = TestObject(snakeCase = "hss")
		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasNoJsonPath("$.snakeCase"))
		assertThat(json, hasJsonPath("$.snake_case", equalTo("hss")))

		val value = mapper.readValue(json, TestObject::class.java)

		assertJThat(value.snakeCase).isEqualTo("hss")
	}


	data class BitfieldTestObject(
		val messageFlags: IntBitfield<MessageFlag>?,
		val permissions: StringBitfield<Permission>?,
	)

	@Test
	internal fun intBitfield() {
		val payload = BitfieldTestObject(IntBitfield(listOf(CROSSPOSTED, EPHEMERAL, URGENT, FAILED_TO_MENTION_SOME_ROLES_IN_THREAD)), null)

		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasNoJsonPath("$.permissions"))
		assertThat(json, hasJsonPath("$.message_flags", equalTo("101010001".toInt(2))))

		val value = mapper.readValue(json, BitfieldTestObject::class.java)
		assertJThat(value.permissions).isNull()
		assertJThat(value.messageFlags).containsExactlyInAnyOrder(CROSSPOSTED, EPHEMERAL, URGENT, FAILED_TO_MENTION_SOME_ROLES_IN_THREAD)
	}


	@Test
	internal fun stringBitfield() {
		val payload = BitfieldTestObject(null, StringBitfield(listOf(CREATE_INSTANT_INVITE, ADMINISTRATOR, MODERATE_MEMBERS)))

		val json = mapper.writeValueAsString(payload)

		assertThat(json, hasNoJsonPath("$.message_flags"))
		assertThat(json, hasJsonPath("$.permissions", equalTo("10000000000000000000000000000000000001001".toBigInteger(2).toString())))

		val value = mapper.readValue(json, BitfieldTestObject::class.java)
		assertJThat(value.messageFlags).isNull()
		assertJThat(value.permissions).containsExactlyInAnyOrder(CREATE_INSTANT_INVITE, ADMINISTRATOR, MODERATE_MEMBERS)
	}
}
