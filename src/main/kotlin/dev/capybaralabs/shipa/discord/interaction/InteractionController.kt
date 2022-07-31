package dev.capybaralabs.shipa.discord.interaction

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.discord.interaction.command.ApplicationCommandService
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.UntypedInteractionObject
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
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

		val response: InteractionResponse = when (val interaction = mapper.readValue(rawBody, UntypedInteractionObject::class.java).typed()) {
			is InteractionObject.Ping -> InteractionResponse.Pong
			is InteractionWithData -> applicationCommandService.onInteraction(interaction)
		}

		return ResponseEntity.ok().body(response)
	}

}
