package dev.capybaralabs.shipa.discord.interaction.validation

import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.logger
import io.prometheus.client.Collector

class InstrumentedInteractionValidator(
	private val label: String,
	private val delegate: InteractionValidator,
	private val metrics: ShipaMetrics,
) : InteractionValidator {

	override fun validateSignature(signature: String, timestamp: String, body: String): Boolean {
		var result = false
		val start = System.nanoTime()
		try {
			result = delegate.validateSignature(signature, timestamp, body)
		} finally {
			val duration = System.nanoTime() - start
			metrics.interactionSignatureValidationTime.labels(label, "$result")
				.observe(duration / Collector.NANOSECONDS_PER_SECOND)
			val ms = duration / 1E6 // nanos per millisecond
			logger().debug(
				"Validated interaction using {} in {}ms: {} {} {} {}",
				label, ms, result, signature, timestamp, body
			)
		}
		return result
	}
}
