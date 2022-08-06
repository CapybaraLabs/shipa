package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.command.CommandRegisterService
import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import kotlinx.coroutines.runBlocking
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class Launcher(
	private val registerService: CommandRegisterService,
	private val commands: List<InteractionCommand>,
) {

	init {
		Thread.setDefaultUncaughtExceptionHandler { t, e -> logger().warn("Uncaught exception in thread {}", t.name, e) }
		println("Henlo")

		runBlocking {
			for (command in commands) {
				registerService.register(command.creation())
			}
		}
	}
}

fun main(args: Array<String>) {
	System.setProperty("spring.config.name", "shipa")
	val app = SpringApplication(Launcher::class.java)
	app.setAdditionalProfiles("secrets")
	app.run(*args)
}
