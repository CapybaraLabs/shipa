package dev.capybaralabs.shipa.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.ApplicationTest
import dev.capybaralabs.shipa.discord.gateway.Heartbeat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class GatewaySerdeTest : ApplicationTest() {

	@Autowired
	private lateinit var mapper: ObjectMapper

	@Test
	fun serializeHeartbeat() {
		val nullSequenceHeartbeat = Heartbeat(null)

		val nullJson = mapper.writeValueAsString(nullSequenceHeartbeat)
		assertThat(nullJson).isEqualTo("null")


		val sequencedHeartbeat = Heartbeat(42)
		val sequenceJson = mapper.writeValueAsString(sequencedHeartbeat)
		assertThat(sequenceJson).isEqualTo("42")
	}
}
