package dev.capybaralabs.shipa.discord

import dev.capybaralabs.shipa.discord.interaction.InteractionValidator
import dev.capybaralabs.shipa.logger
import javax.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/interaction")
class InteractionController(private val interactionValidator: InteractionValidator) {

	@PostMapping
	fun post(req: HttpServletRequest, @RequestBody rawBody: String): ResponseEntity<Void> {
		val signature = req.getHeader("X-Signature-Ed25519")
		val timestamp = req.getHeader("X-Signature-Timestamp")

		if (!interactionValidator.validateSignature(signature, timestamp, rawBody)) {
			logger().info("Nope")
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
		}

		logger().info("Yep")
		return ResponseEntity.ok().build()
	}

}
