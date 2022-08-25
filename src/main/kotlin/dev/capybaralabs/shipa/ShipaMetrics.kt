package dev.capybaralabs.shipa

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import io.prometheus.client.Summary
import org.springframework.stereotype.Component

@Component
class ShipaMetrics(collectorRegistry: CollectorRegistry) {

	final val interactionSignatureValidationTime: Summary
	final val interactionHttpResponseTime: Summary
	final val interactionTotalTime: Summary
	final val commandProcessTime: Summary

	final val discordRestRequests: Summary
	final val discordRestRequestResponseTime: Histogram
	final val discordRestHardFailures: Counter

	init {
		interactionSignatureValidationTime = Summary.build()
			.name("shipa_interaction_signature_validation_seconds")
			.help("How long signature validation for interactions takes")
			.labelNames("implementation", "success")
			.register(collectorRegistry)

		interactionHttpResponseTime = Summary.build()
			.name("shipa_interaction_http_response_time_seconds")
			.help("How long until we return an http response to discord")
			.register(collectorRegistry)

		interactionTotalTime = Summary.build()
			.name("shipa_interaction_total_time_seconds")
			.help("How long until an interaction is fully processed")
			.register(collectorRegistry)

		commandProcessTime = Summary.build()
			.name("shipa_command_process_time_seconds")
			.help("How long until an interaction is fully processed")
			.labelNames("name", "type")
			.register(collectorRegistry)

		discordRestRequests = Summary.build()
			.name("shipa_discord_rest_request_seconds")
			.help("Total Discord REST requests sent and their received responses")
			.labelNames("method", "uri", "status", "error")
			.register(collectorRegistry)

		discordRestRequestResponseTime = Histogram.build()
			.name("shipa_discord_rest_request_response_time_seconds")
			.exponentialBuckets(0.05, 1.2, 20)
			.help("Discord REST request response time")
			.register(collectorRegistry)

		discordRestHardFailures = Counter.build()
			.name("shipa_discord_rest_request_hard_failures_total")
			.help("Total Discord REST requests that experienced hard failures (not client response exceptions)")
			.labelNames("method", "uri")
			.register(collectorRegistry)
	}

}
