package dev.capybaralabs.shipa.discord.interaction

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.discord.interaction.command.ApplicationCommandService
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.UntypedInteractionObject
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import io.prometheus.client.Summary
import java.util.concurrent.TimeUnit.SECONDS
import javax.servlet.http.HttpServletRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val HEADER_SIGNATURE = "X-Signature-Ed25519"
const val HEADER_TIMESTAMP = "X-Signature-Timestamp"

@RestController
@RequestMapping("\${shipa.interaction-controller-path:/api/interaction}")
internal class InteractionController(
	private val interactionValidator: InteractionValidator,
	private val mapper: ObjectMapper,
	private val applicationCommandService: ApplicationCommandService,
	private val interactionScope: CoroutineScope,
	private val metrics: ShipaMetrics,
) {

	private val log = LoggerFactory.getLogger(InteractionController::class.java)

	@PostMapping
	fun post(req: HttpServletRequest, @RequestBody rawBody: String): ResponseEntity<InteractionResponse> {
		metrics.interactionHttpResponseTime.startTimer().use {
			return doPost(req, rawBody)
		}
	}

	private fun doPost(req: HttpServletRequest, rawBody: String): ResponseEntity<InteractionResponse> {
		val totalTimer = metrics.interactionTotalTime.startTimer()
		val signature: String? = req.getHeader(HEADER_SIGNATURE)
		val timestamp: String? = req.getHeader(HEADER_TIMESTAMP)
		if (signature == null || timestamp == null) {
			return ResponseEntity.badRequest().build()
		}

		if (!interactionValidator.validateSignature(signature, timestamp, rawBody)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
		}

		val interaction = mapper.readValue(rawBody, UntypedInteractionObject::class.java).typed()
		val result = CompletableDeferred<InteractionResponse>()

		interactionScope.launchInteractionProcessing(interaction, result, totalTimer)
		return result.asCompletableFuture()
			.thenApply { ResponseEntity.ok().body(it) }
			.orTimeout(3, SECONDS)
			.join()

	}

	private fun CoroutineScope.launchInteractionProcessing(interaction: InteractionObject, result: CompletableDeferred<InteractionResponse>, totalTimer: Summary.Timer) =
		launch(CoroutineExceptionHandler { _, t -> log.error("Unhandled exception in coroutine", t) }) {
			log.debug("Launching interaction processing coroutine!")

			try {
				when (interaction) {
					is InteractionObject.Ping -> result.complete(InteractionResponse.Pong)
					is InteractionWithData -> applicationCommandService.onInteraction(interaction, result)
				}
			} catch (t: Throwable) {
				log.error("Uncaught error processing the interaction", t)
				result.completeExceptionally(t)
			} finally {
				totalTimer.observeDuration()
			}
		}
}
