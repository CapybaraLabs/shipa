package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.discord.delay
import dev.capybaralabs.shipa.discord.interaction.command.ApplicationCommandService
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.ShipaMetadata
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.discord.interaction.model.UntypedInteractionObject
import dev.capybaralabs.shipa.discord.interaction.validation.InteractionValidator
import dev.capybaralabs.shipa.discord.millis
import dev.capybaralabs.shipa.discord.time
import dev.capybaralabs.shipa.jackson.ShipaJsonMapper
import dev.capybaralabs.shipa.logger
import io.micrometer.core.instrument.Timer
import io.sentry.DateUtils
import io.sentry.kotlin.SentryContext
import jakarta.servlet.http.HttpServletRequest
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
	private val shipaJsonMapper: ShipaJsonMapper,
	private val applicationCommandService: ApplicationCommandService,
	private val interactionScope: CoroutineScope,
	private val metrics: ShipaMetrics,
) {

	private val log = LoggerFactory.getLogger(InteractionController::class.java)

	/**
	 * @param rawBody [UntypedInteractionObject] as JSON, later refined to [InteractionObject]
	 * @return [InteractionResponse] as JSON
	 */
	@PostMapping
	fun post(req: HttpServletRequest, @RequestBody rawBody: String): CompletionStage<ResponseEntity<String>> {
		logger().debug("Incoming interaction with body {}", rawBody)
		return metrics.interactionHttpResponseTime().time { doPost(req, rawBody) }
	}

	private fun doPost(req: HttpServletRequest, rawBody: String): CompletionStage<ResponseEntity<String>> {
		val totalTimer = Timer.start()
		val requestReceived = Instant.now() //would be nice if we could access an even earlier timestamp from the underlying webserver
		val signature: String? = req.getHeader(HEADER_SIGNATURE)
		val timestamp: String? = req.getHeader(HEADER_TIMESTAMP)
		if (signature == null || timestamp == null) {
			return CompletableFuture.completedStage(ResponseEntity.badRequest().build())
		}

		if (!interactionValidator.validateSignature(signature, timestamp, rawBody)) {
			return CompletableFuture.completedStage(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
		}

		val untypedInteraction = shipaJsonMapper.mapper.readValue(rawBody, UntypedInteractionObject::class.java)

		val discordTimestamp = timestamp.toLongOrNull()?.let { Instant.ofEpochSecond(it) }
		if (discordTimestamp == null) {
			logger().warn("Interaction {}: Request timestamp header is not a number: {}", untypedInteraction.id, timestamp)
		} else {
			checkTimestamp(discordTimestamp, requestReceived, untypedInteraction)
			// continue processing, just log to start collecting data.
			// discord does not state anything about having to check the timestamp recency.
		}

		val diff = discordTimestamp?.let { Duration.between(discordTimestamp, requestReceived) }
		log.trace(
			"Interaction {}: Discord timestamp header: {}, received time: {}, diff {}ms",
			untypedInteraction.id,
			discordTimestamp,
			requestReceived,
			diff?.toMillis(),
		)

		val metadata = ShipaMetadata(discordTimestamp ?: requestReceived)
		val interaction = untypedInteraction.typed(metadata)
		val result = CompletableDeferred<InteractionResponse>()
		val resultSent = CompletableFuture<Unit>()
			.orTimeout(10, SECONDS)
			.exceptionallyCompose {
				if (it is TimeoutException) {
					CompletableFuture.failedFuture(TimeoutException("resultSent Future timed out on interaction ${interaction.id}"))
				} else {
					CompletableFuture.failedFuture(it)
				}
			}

		val initialResponse = InitialResponse(
			result,
			resultSent
				.thenCompose {
					// give Discord some time to process our response before we start sending followup requests
					interactionScope.async { delay(200.millis) }.asCompletableFuture()
				},
		)
		req.setAttribute(CompletionInterceptor.ATTRIBUTE, resultSent)

		interactionScope.launchInteractionProcessing(interaction, initialResponse, totalTimer)
		return result.asCompletableFuture()
			.thenApply {
				metrics.interactionCallbackTypes(it).increment()
				val responseJson = shipaJsonMapper.mapper.writeValueAsString(it)
				log.debug("Interaction {}: Responding with {}", interaction.id, responseJson)
				ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(responseJson)
			}
			.orTimeout(3, SECONDS) // consider timing out based on Discord timestamp, not our own.
			.exceptionallyCompose {
				if (it is TimeoutException) {
					CompletableFuture.failedFuture(TimeoutException("result Future timed out on interaction ${interaction.id}"))
				} else {
					CompletableFuture.failedFuture(it)
				}
			}
	}

	private fun checkTimestamp(discordTimestamp: Instant, requestReceived: Instant, untypedInteraction: UntypedInteractionObject) {
		val diff = Duration.between(discordTimestamp, requestReceived)
		if (diff.isNegative) {
			// discord timestamp has seconds resolution. so we round our own to the nearest seconds to avoid warnings like
			// Request timestamp header is too young: header 2025-07-16T07:51:25Z vs our clock: 2025-07-16T07:51:24.996183346
			val millisPart = requestReceived.toEpochMilli() % 1000
			val rounded = requestReceived.truncatedTo(ChronoUnit.SECONDS).plusSeconds(
				if (millisPart >= 500) 1 else 0,
			)
			val roundedDiff = Duration.between(discordTimestamp, rounded)
			if (roundedDiff.isNegative) {
				logger().warn(
					"Interaction {}: Request timestamp header is too young: header {} vs our clock: {}",
					untypedInteraction.id, discordTimestamp, requestReceived,
				)
				// cannot record the interaction time, as micrometer does not support negative times
			} else {
				metrics.interactionDiffTime().record(roundedDiff)
			}
		} else {
			metrics.interactionDiffTime().record(diff)
		}
		if (diff > Duration.ofSeconds(10)) {
			logger().warn(
				"Interaction {}: Request timestamp header is fairly old: header {} vs our clock: {}",
				untypedInteraction.id, discordTimestamp, requestReceived,
			)
		}
	}

	private fun CoroutineScope.launchInteractionProcessing(interaction: InteractionObject, initialResponse: InitialResponse, totalTimer: Timer.Sample) =
		launch(SentryContext() + CoroutineExceptionHandler { _, t -> log.error("Unhandled exception in coroutine", t) }) {
			log.trace("Interaction {}: Launching interaction processing coroutine!", interaction.id)

			try {
				when (interaction) {
					is InteractionObject.Ping -> initialResponse.complete(InteractionResponse.Pong)
					is InteractionWithData -> applicationCommandService.onInteraction(interaction, initialResponse)
				}
			} catch (e: Exception) {
				log.error("Uncaught error processing interaction {}", interaction.id, e)
				initialResponse.completeExceptionally(e)
			} finally {
				log.trace("Interaction {}: Done processing", interaction.id)
				totalTimer.stop(metrics.interactionTotalTime())
			}
		}
}
