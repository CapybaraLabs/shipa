package dev.capybaralabs.shipa.discord.interaction.validation

import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.ShipaMetrics.Companion.NANOSECONDS_PER_MILLISECOND
import dev.capybaralabs.shipa.logger
import io.micrometer.core.instrument.Timer

class InstrumentedInteractionValidator(
	private val label: String,
	private val delegate: InteractionValidator,
	private val metrics: ShipaMetrics,
) : InteractionValidator {

	override fun validateSignature(signature: String, timestamp: String, body: String): Boolean {
		var result = false
		val timer = Timer.start()
		try {
			result = delegate.validateSignature(signature, timestamp, body)
		} finally {
			val durationNanos = timer.stop(metrics.interactionSignatureValidationTime(label, "$result"))
			val ms = durationNanos / NANOSECONDS_PER_MILLISECOND
			logger().debug(
				"Validated interaction using {} in {}ms: {} {} {} {}",
				label, ms, result, signature, timestamp, body,
			)
		}
		return result
	}
}
