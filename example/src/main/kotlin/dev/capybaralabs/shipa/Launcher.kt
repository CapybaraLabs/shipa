package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.command.CommandRegisterService
import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import io.prometheus.client.CollectorRegistry
import kotlinx.coroutines.runBlocking
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class Launcher(
	private val registerService: CommandRegisterService,
	private val commands: List<InteractionCommand>,
	collectorRegistry: CollectorRegistry,
) {

	init {
		// https://github.com/prometheus/client_java/issues/279
		collectorRegistry.clear()

		Thread.setDefaultUncaughtExceptionHandler { t, e -> logger().warn("Uncaught exception in thread {}", t.name, e) }
		println("Henlo")

		runBlocking {
			registerService.bulkOverwrite(commands.map { it.creation() })
		}
	}
}

fun main(args: Array<String>) {
	System.setProperty("spring.config.name", "shipa")
	val app = SpringApplication(Launcher::class.java)
	app.setAdditionalProfiles("secrets")
	app.run(*args)
}
