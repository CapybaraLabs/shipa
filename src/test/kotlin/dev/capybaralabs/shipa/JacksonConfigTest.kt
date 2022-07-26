package dev.capybaralabs.shipa

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath
import java.util.Optional
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.assertj.core.api.Assertions.assertThat as assertJThat

/**
 * Making extensive use of [json-path-assert](https://github.com/json-path/JsonPath/tree/master/json-path-assert) here.
 */
internal class JacksonConfigTest : ApplicationTest() {

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	data class TestObject(
		val regular: String = "foo",
		val optional: String? = null,
		val nullable: Optional<String> = Optional.empty(),
		val optionalNullable: Optional<String>? = null,
		val snakeCase: String = "hssssss"
	)

	@Test
	internal fun baseline() {
		val payload = TestObject()
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.regular", equalTo("foo")))
		assertThat(json, hasNoJsonPath("$.optional"))
		assertThat(json, hasJsonPath("$.nullable", nullValue()))
		assertThat(json, hasNoJsonPath("$.optional_nullable"))

		val value = objectMapper.readValue(json, TestObject::class.java)

		assertJThat(value.regular).isEqualTo("foo")
		assertJThat(value.optional).isNull()
		assertJThat(value.nullable).isEmpty
		assertJThat(value.optionalNullable).isNull()
	}


	@Test
	internal fun givenOptionalFieldIsNull_whenSerialized_isMissing() {
		val payload = TestObject(optional = null)
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasNoJsonPath("$.optional"))

		val value = objectMapper.readValue(json, TestObject::class.java)
		assertJThat(value.optional).isNull()
	}

	@Test
	internal fun givenOptionalFieldHasValue_whenSerialized_isPresentWithValue() {
		val payload = TestObject(optional = "bar")
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.optional", equalTo("bar")))

		val value = objectMapper.readValue(json, TestObject::class.java)
		assertJThat(value.optional).isEqualTo("bar")
	}


	@Test
	internal fun givenNullableFieldIsEmpty_whenSerialized_isPresentWithNull() {
		val payload = TestObject(nullable = Optional.empty())
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.nullable", nullValue()))

		val value = objectMapper.readValue(json, TestObject::class.java)
		assertJThat(value.nullable).isEmpty
	}

	@Test
	internal fun givenNullableFieldHasValue_whenSerialized_isPresentWithValue() {
		val payload = TestObject(nullable = Optional.of("bar"))
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.nullable", equalTo("bar")))

		val value = objectMapper.readValue(json, TestObject::class.java)
		assertJThat(value.nullable).hasValue("bar")
	}


	@Test
	internal fun givenOptionalNullableFieldIsNull_whenSerialized_isMissing() {
		val payload = TestObject(optionalNullable = null)
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasNoJsonPath("$.optional_nullable"))

		val value = objectMapper.readValue(json, TestObject::class.java)
		assertJThat(value.optionalNullable).isNull()
	}

	@Test
	internal fun givenOptionalNullableFieldIsEmpty_whenSerialized_isPresentWithNull() {
		val payload = TestObject(optionalNullable = Optional.empty())
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.optional_nullable", nullValue()))

		val value = objectMapper.readValue(json, TestObject::class.java)
		assertJThat(value.optionalNullable).isEmpty
	}

	@Test
	internal fun givenOptionalNullableFieldHasValue_whenSerialized_isPresentWithValue() {
		val payload = TestObject(optionalNullable = Optional.of("bar"))
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasJsonPath("$.optional_nullable", equalTo("bar")))

		val value = objectMapper.readValue(json, TestObject::class.java)
		assertJThat(value.optionalNullable).hasValue("bar")
	}

	@Test
	internal fun givenCamelCaseProperty_whenSerialized_writeSnakeCase() {
		val payload = TestObject(snakeCase = "hss")
		val json = objectMapper.writeValueAsString(payload)

		assertThat(json, hasNoJsonPath("$.snakeCase"))
		assertThat(json, hasJsonPath("$.snake_case", equalTo("hss")))

		val value = objectMapper.readValue(json, TestObject::class.java)

		assertJThat(value.snakeCase).isEqualTo("hss")
	}

}
