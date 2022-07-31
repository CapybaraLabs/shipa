package dev.capybaralabs.shipa.discord.interaction

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.discord.interaction.command.ApplicationCommandService
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.SendMessage
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse.UpdateMessage
import dev.capybaralabs.shipa.discord.interaction.model.UntypedInteractionObject
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import dev.capybaralabs.shipa.logger
import javax.servlet.http.HttpServletRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val HEADER_SIGNATURE = "X-Signature-Ed25519"
const val HEADER_TIMESTAMP = "X-Signature-Timestamp"

@RestController
@RequestMapping("\${shipa.controller-path:/api}/interaction")
class InteractionController(
	private val interactionValidator: InteractionValidator,
	@Suppress("SpringJavaInjectionPointsAutowiringInspection") private val mapper: ObjectMapper,
	private val applicationCommandService: ApplicationCommandService,
	private val restService: InteractionRestService,
	private val interactionScope: CoroutineScope,
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

		val interaction = mapper.readValue(rawBody, UntypedInteractionObject::class.java).typed()
		val response = when (interaction) {
			is InteractionObject.Ping -> sequenceOf(InteractionResponse.Pong)
			is InteractionWithData -> applicationCommandService.onInteraction(interaction)
		}

		val iterator = response.iterator()
		val initialResponse = iterator.next()

		// TODO order of responses? how to ensure ACK is dispatched first?
		interactionScope.launchInteractionProcessing(interaction.token, iterator)

		logger().debug("Returning initial response $initialResponse")
		return ResponseEntity.ok().body(initialResponse)
	}

	fun CoroutineScope.launchInteractionProcessing(interactionToken: String, responses: Iterator<InteractionResponse>) = launch {
		while (responses.hasNext()) {
			val nextResponse = responses.next()
			logger().debug("Dispatching additional response $nextResponse")
			if (nextResponse is UpdateMessage) {
				restService.editOriginalResponse(interactionToken, nextResponse.data)
			}
			if (nextResponse is SendMessage) {
				restService.createFollowupMessage(interactionToken, nextResponse.data)
			}
		}
		logger().debug("Done processing additional responses")
	}
}
