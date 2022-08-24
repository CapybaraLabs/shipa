package dev.capybaralabs.shipa

import io.prometheus.client.CollectorRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfiguration {

	@Bean
	fun collectorRegistry(): CollectorRegistry {
		return CollectorRegistry.defaultRegistry
	}
}
