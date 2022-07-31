package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.command.CommandRegisterService
import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import javax.annotation.PostConstruct
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class Launcher(
	private val registerService: CommandRegisterService,
	private val commands: List<InteractionCommand>,
) {

	private val debugGuildId: Long = 214539058028740609L

	init {
		Thread.setDefaultUncaughtExceptionHandler { t, e -> logger().warn("Uncaught exception in thread {}", t.name, e) }
		println("Henlo")
	}

	@PostConstruct // TODO consider putting this into some kind of autoconfig
	fun setup() {
		for (command in commands) {
			registerService.register(command.create, debugGuildId)
		}
	}
}

fun main(args: Array<String>) {
	System.setProperty("spring.config.name", "shipa")
	val app = SpringApplication(Launcher::class.java)
	app.setAdditionalProfiles("secrets")
	app.run(*args)
}
