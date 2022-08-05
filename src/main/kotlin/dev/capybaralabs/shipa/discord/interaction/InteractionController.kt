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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedStage
import java.util.concurrent.CompletionStage
import javax.servlet.http.HttpServletRequest
import kotlinx.coroutines.CoroutineExceptionHandler
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
internal class InteractionController(
	private val interactionValidator: InteractionValidator,
	@Suppress("SpringJavaInjectionPointsAutowiringInspection") private val mapper: ObjectMapper,
	private val applicationCommandService: ApplicationCommandService,
	private val restService: InteractionRestService,
	private val interactionScope: CoroutineScope,
) {

	@PostMapping
	fun post(req: HttpServletRequest, @RequestBody rawBody: String): CompletionStage<ResponseEntity<InteractionResponse>> {
		val signature: String? = req.getHeader(HEADER_SIGNATURE)
		val timestamp: String? = req.getHeader(HEADER_TIMESTAMP)
		if (signature == null || timestamp == null) {
			return completedStage(ResponseEntity.badRequest().build())
		}

		if (!interactionValidator.validateSignature(signature, timestamp, rawBody)) {
			return completedStage(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
		}

		val interaction = mapper.readValue(rawBody, UntypedInteractionObject::class.java).typed()
		val result = CompletableFuture<ResponseEntity<InteractionResponse>>()

		interactionScope.launchInteractionProcessing(interaction, result)
		return result
	}

	private fun CoroutineScope.launchInteractionProcessing(interaction: InteractionObject, result: CompletableFuture<ResponseEntity<InteractionResponse>>) =
		launch(CoroutineExceptionHandler { _, t -> logger().error("Unhandled exception in coroutine", t) }) {
			logger().debug("Launching interaction processing coroutine!")

			val response = when (interaction) {
				is InteractionObject.Ping -> sequenceOf(InteractionResponse.Pong)
				is InteractionWithData -> applicationCommandService.onInteraction(interaction)
			}

			val iterator = response.iterator()
			val initialResponse = iterator.next()

			logger().debug("Returning initial response $initialResponse")
			result.complete(ResponseEntity.ok().body(initialResponse))

			while (iterator.hasNext()) {
				val nextResponse = iterator.next()
				logger().debug("Dispatching additional response $nextResponse")
				if (nextResponse is UpdateMessage) {
					restService.editOriginalResponse(interaction.token, nextResponse.data)
				}
				if (nextResponse is SendMessage) {
					restService.createFollowupMessage(interaction.token, nextResponse.data)
				}
			}
			logger().debug("Done processing additional responses")
		}
}
