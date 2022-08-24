package dev.capybaralabs.shipa

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Summary
import org.springframework.stereotype.Component

@Component
class ShipaMetrics(collectorRegistry: CollectorRegistry) {

	final val interactionSignatureValidationTime: Summary
	final val interactionHttpResponseTime: Summary
	final val commandProcessTime: Summary

	final val interactionTotalTime: Summary

	init {
		interactionSignatureValidationTime = Summary.build()
			.name("shipa_interaction_signature_validation_seconds")
			.help("How long signature validation for interactions takes")
			.labelNames("implementation", "success")
			.register(collectorRegistry)

		interactionHttpResponseTime = Summary.build()
			.name("shipa_interaction_http_response_time_seconds")
			.help("How long until we return an http response to discord")
			.register()

		interactionTotalTime = Summary.build()
			.name("shipa_interaction_total_time_seconds")
			.help("How long until an interaction is fully processed")
			.register()

		commandProcessTime = Summary.build()
			.name("shipa_command_process_time_seconds")
			.help("How long until an interaction is fully processed")
			.labelNames("name", "type")
			.register()
	}

}
