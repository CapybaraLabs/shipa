package dev.capybaralabs.shipa

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import java.time.Duration
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import org.springframework.stereotype.Component

@Component
class ShipaMetrics(private val meterRegistry: MeterRegistry) {

	companion object {
		const val NANOSECONDS_PER_MILLISECOND: Double = 1_000_000.0
	}

	fun interactionSignatureValidationTime(implementation: String, success: String): Timer {
		return Timer
			.builder("shipa_interaction_signature_validation_seconds")
			.description("How long signature validation for interactions takes")
			.tags(
				Tags.of(
					Tag.of("implementation", implementation),
					Tag.of("success", success),
				),
			)
			.register(meterRegistry)
	}

	fun interactionHttpResponseTime(): Timer {
		return Timer
			.builder("shipa_interaction_http_response_time_seconds")
			.description("How long until we return an http response to discord")
			.register(meterRegistry)
	}

	fun interactionTotalTime(): Timer {
		return Timer
			.builder("shipa_interaction_total_time_seconds")
			.description("How long until an interaction is fully processed")
			.register(meterRegistry)
	}

	fun commandProcessTime(name: String, type: String): Timer {
		return Timer
			.builder("shipa_command_process_time_seconds")
			.description("How long until an interaction is fully processed")
			.tags(
				Tags.of(
					Tag.of("name", name),
					Tag.of("type", type),
				),
			)
			.register(meterRegistry)
	}

	fun discordRestRequests(method: String, uri: String, status: String, error: String): Timer {
		return Timer
			.builder("shipa_discord_rest_request_seconds")
			.description("Total Discord REST requests sent and their received responses")
			.tags(
				Tags.of(
					Tag.of("method", method),
					Tag.of("uri", uri),
					Tag.of("status", status),
					Tag.of("error", error),
				),
			)
			.register(meterRegistry)
	}

	fun discordRestRequestResponseTime(): Timer {
		return Timer
			.builder("shipa_discord_rest_request_response_time_seconds")
			.description("Discord REST request response time")
			.serviceLevelObjectives(*exponentialBuckets(50.milliseconds.toJavaDuration(), 1.2, 20))
			.publishPercentileHistogram()
			.register(meterRegistry)
	}

	fun discordRestHardFailures(method: String, uri: String): Counter {
		return Counter
			.builder("shipa_discord_rest_request_hard_failures_total")
			.description("Total Discord REST requests that experienced hard failures (not client response exceptions)")
			.tags(
				Tags.of(
					Tag.of("method", method),
					Tag.of("uri", uri),
				),
			)
			.register(meterRegistry)
	}


	// mimics former functionality of prometheus simple client
	private fun exponentialBuckets(start: Duration, factor: Double, amount: Int): Array<Duration> {
		return IntRange(0, amount).map { i ->
			(start.toMillis() * factor.pow(i.toDouble()))
				.milliseconds.toJavaDuration()
		}.toTypedArray()
	}

}
