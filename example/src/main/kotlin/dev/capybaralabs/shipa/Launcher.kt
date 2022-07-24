package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.CommandRegisterService
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateUserCommand
import javax.annotation.PostConstruct
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class Launcher(private val registerService: CommandRegisterService) {

	private val debugGuildId: Long = 214539058028740609L

	init {
		println("Henlo")
	}

	@PostConstruct
	fun setup() {
		val command = CreateUserCommand("henlo")

		registerService.register(command, debugGuildId)
	}
}

fun main(args: Array<String>) {
	System.setProperty("spring.config.name", "shipa")
	val app = SpringApplication(Launcher::class.java)
	app.setAdditionalProfiles("secrets")
	app.run(*args)
}
