package dev.capybaralabs.shipa

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class Launcher {

	init {
		println("Henlo")
	}
}

fun main(args: Array<String>) {
	System.setProperty("spring.config.name", "shipa")
	val app = SpringApplication(Launcher::class.java)
	app.setAdditionalProfiles("secrets")
	app.run(*args)
}
