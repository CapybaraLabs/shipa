package dev.capybaralabs.shipa.discord.interaction

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.discord.interaction.command.ApplicationCommandService
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.UntypedInteractionObject
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import dev.capybaralabs.shipa.discord.time
import dev.capybaralabs.shipa.logger
import io.micrometer.core.instrument.Timer
import io.sentry.kotlin.SentryContext
import jakarta.servlet.http.HttpServletRequest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
	fun post(req: HttpServletRequest, @RequestBody rawBody: String): CompletionStage<ResponseEntity<InteractionResponse>> {
		logger().debug("Incoming interaction with body {}", rawBody)
		return metrics.interactionHttpResponseTime().time { doPost(req, rawBody) }
	}

	private fun doPost(req: HttpServletRequest, rawBody: String): CompletionStage<ResponseEntity<InteractionResponse>> {
		val totalTimer = Timer.start()
		val signature: String? = req.getHeader(HEADER_SIGNATURE)
		val timestamp: String? = req.getHeader(HEADER_TIMESTAMP)
		if (signature == null || timestamp == null) {
			return CompletableFuture.completedStage(ResponseEntity.badRequest().build())
		}

		if (!interactionValidator.validateSignature(signature, timestamp, rawBody)) {
			return CompletableFuture.completedStage(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
		}

		val interaction = mapper.readValue(rawBody, UntypedInteractionObject::class.java).typed()
		val result = CompletableDeferred<InteractionResponse>()
		val resultSent = CompletableFuture<Unit>().orTimeout(10, SECONDS)

		val initialResponse = InitialResponse(
			result,
			resultSent
				.thenCompose {
					// give Discord some time to process our response before we start sending followup requests
					interactionScope.async { delay(200.milliseconds) }.asCompletableFuture()
				},
		)
		req.setAttribute(CompletionInterceptor.ATTRIBUTE, resultSent)

		interactionScope.launchInteractionProcessing(interaction, initialResponse, totalTimer)
		return result.asCompletableFuture()
			.thenApply { ResponseEntity.ok().body(it) }
			.orTimeout(3, SECONDS)
	}

	private fun CoroutineScope.launchInteractionProcessing(interaction: InteractionObject, initialResponse: InitialResponse, totalTimer: Timer.Sample) =
		launch(SentryContext() + CoroutineExceptionHandler { _, t -> log.error("Unhandled exception in coroutine", t) }) {
			log.debug("Launching interaction processing coroutine!")

			try {
				when (interaction) {
					is InteractionObject.Ping -> initialResponse.complete(InteractionResponse.Pong)
					is InteractionWithData -> applicationCommandService.onInteraction(interaction, initialResponse)
				}
			} catch (e: Exception) {
				log.error("Uncaught error processing the interaction", e)
				initialResponse.completeExceptionally(e)
			} finally {
				totalTimer.stop(metrics.interactionTotalTime())
			}
		}
}
