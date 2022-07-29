package dev.capybaralabs.shipa.discord

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.discord.interaction.ApplicationCommandService
import dev.capybaralabs.shipa.discord.interaction.InteractionValidator
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.APPLICATION_COMMAND
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.PING
import dev.capybaralabs.shipa.logger
import javax.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val HEADER_SIGNATURE = "X-Signature-Ed25519"
const val HEADER_TIMESTAMP = "X-Signature-Timestamp"

@RestController
@RequestMapping("/api/interaction")
class InteractionController(
	private val interactionValidator: InteractionValidator,
	private val mapper: ObjectMapper,
	private val applicationCommandService: ApplicationCommandService,
) {

	@PostMapping
	fun post(req: HttpServletRequest, @RequestBody rawBody: String): ResponseEntity<InteractionResponse> {
		val signature: String? = req.getHeader(HEADER_SIGNATURE)
		val timestamp: String? = req.getHeader(HEADER_TIMESTAMP)
		if (signature == null || timestamp == null) {
			return ResponseEntity.badRequest().build()
		}

		if (!interactionValidator.validateSignature(signature, timestamp, rawBody)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
		}

		val interaction = mapper.readValue(rawBody, InteractionObject::class.java)
		return when (interaction.type) {
			PING -> ResponseEntity.ok().body(InteractionResponse.Pong)
			APPLICATION_COMMAND -> ResponseEntity.ok().body(applicationCommandService.onApplicationCommand(interaction))
			else -> {
				logger().warn("Interaction Type ${interaction.type} not implemented!")
				ResponseEntity.internalServerError().build()
			}
		}
	}


}
