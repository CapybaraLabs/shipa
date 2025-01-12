package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.ApplicationTest
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.PONG
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.ShipaMetadata
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean


internal class InteractionControllerTest : ApplicationTest() {

	@MockitoSpyBean
	private lateinit var interactionValidator: InteractionValidator

	private val metadata = ShipaMetadata(Instant.now())

	@Test
	internal fun whenMissingHeaders_badRequest() {
		val headers = HttpHeaders()
		val body = InteractionObject.Ping(42, metadata, 42, "foo", 1)

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), Void::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
	}

	@Test
	internal fun whenMissingSignatureHeader_badRequest() {
		val headers = HttpHeaders().apply {
			add(HEADER_TIMESTAMP, "bar")
		}
		val body = InteractionObject.Ping(42, metadata, 42, "foo", 1)

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), Void::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
	}


	@Test
	internal fun whenMissingTimestampHeader_badRequest() {
		val headers = HttpHeaders().apply {
			add(HEADER_SIGNATURE, "foo")
		}
		val body = InteractionObject.Ping(42, metadata, 42, "foo", 1)

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), Void::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
	}

	@Test
	internal fun whenWrongHeaders_unauthorized() {
		val headers = HttpHeaders().apply {
			add(HEADER_SIGNATURE, "foo")
			add(HEADER_TIMESTAMP, "bar")
		}
		val body = InteractionObject.Ping(42, metadata, 42, "foo", 1)

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), Void::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
	}

	@Test
	internal fun whenPing_thenPong() {
		val headers = HttpHeaders().apply {
			add(HEADER_SIGNATURE, "signature")
			add(HEADER_TIMESTAMP, "timestamp")
		}
		val body = InteractionObject.Ping(42, metadata, 42, "foo", 1)

		doReturn(true).`when`(interactionValidator).validateSignature(eq("signature"), eq("timestamp"), any())

		val response = this.testRestTemplate.postForEntity("/api/interaction", HttpEntity(body, headers), InteractionResponse.Pong::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(response.body).isNotNull
		assertThat(response.body!!.type).isEqualTo(PONG)
	}

}
