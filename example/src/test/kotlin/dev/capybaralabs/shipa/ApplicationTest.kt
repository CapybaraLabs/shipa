package dev.capybaralabs.shipa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = ["spring.config.name=shipa"])
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
internal abstract class ApplicationTest {

	@LocalServerPort
	protected var port: Int = 0

	@Autowired
	protected lateinit var testRestTemplate: TestRestTemplate

	@Test
	fun `web application port is assigned`() {
		assertThat(port)
			.withFailMessage("web application port should have been injected")
			.isNotEqualTo(0)
	}
}
