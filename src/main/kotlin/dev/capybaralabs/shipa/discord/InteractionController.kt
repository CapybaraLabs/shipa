package dev.capybaralabs.shipa.discord

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.discord.interaction.InteractionValidator
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackType.PONG
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.PING
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

	private val mapper = ObjectMapper()

	@PostMapping
	fun post(req: HttpServletRequest, @RequestBody rawBody: String): ResponseEntity<InteractionResponse> {
		val signature = req.getHeader("X-Signature-Ed25519")
		val timestamp = req.getHeader("X-Signature-Timestamp")

		if (!interactionValidator.validateSignature(signature, timestamp, rawBody)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
		}

		val node = mapper.readValue(rawBody, JsonNode::class.java)
		val type = InteractionType.fromValue(node.get("type").asInt())

		return when (type) {
			PING -> ResponseEntity.ok().body(InteractionResponse(PONG))
			else -> {
				logger().warn("Interaction Type $type not implemented!")
				ResponseEntity.internalServerError().build()
			}
		}
	}


}
