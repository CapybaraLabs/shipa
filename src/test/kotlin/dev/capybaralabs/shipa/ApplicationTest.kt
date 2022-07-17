package dev.capybaralabs.shipa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
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
