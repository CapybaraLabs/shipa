package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.ApplicationTest
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.PONG
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus


internal class InteractionControllerTest : ApplicationTest() {

	@SpyBean
	private lateinit var interactionValidator: InteractionValidator

	@Test
	internal fun whenMissingHeaders_badRequest() {
		val headers = HttpHeaders()
		val body = InteractionObject.Ping(42, 42)

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), Void::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
	}

	@Test
	internal fun whenMissingSignatureHeader_badRequest() {
		val headers = HttpHeaders()
		headers.add(HEADER_TIMESTAMP, "bar")
		val body = InteractionObject.Ping(42, 42)

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), Void::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
	}


	@Test
	internal fun whenMissingTimestampHeader_badRequest() {
		val headers = HttpHeaders()
		headers.add(HEADER_SIGNATURE, "foo")
		val body = InteractionObject.Ping(42, 42)

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), Void::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
	}

	@Test
	internal fun whenWrongHeaders_unauthorized() {
		val headers = HttpHeaders()
		headers.add(HEADER_SIGNATURE, "foo")
		headers.add(HEADER_TIMESTAMP, "bar")
		val body = InteractionObject.Ping(42, 42)

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), Void::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
	}

	@Test
	internal fun whenPing_thenPong() {
		val headers = HttpHeaders()
		headers.add(HEADER_SIGNATURE, "signature")
		headers.add(HEADER_TIMESTAMP, "timestamp")
		val body = InteractionObject.Ping(42, 42)

		doReturn(true).`when`(interactionValidator).validateSignature(eq("signature"), eq("timestamp"), any())

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), InteractionResponse.Pong::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(response.body).isNotNull
		assertThat(response.body!!.type).isEqualTo(PONG)
	}

}
