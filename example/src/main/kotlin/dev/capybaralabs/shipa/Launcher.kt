package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.entity.DiscordEntityRestService
import dev.capybaralabs.shipa.discord.gateway.GatewayIntent
import dev.capybaralabs.shipa.discord.gateway.GatewayService
import dev.capybaralabs.shipa.discord.gateway.GatewayService.GatewayProps
import dev.capybaralabs.shipa.discord.interaction.command.CommandRegisterService
import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import io.prometheus.client.CollectorRegistry
import java.net.URI
import kotlinx.coroutines.runBlocking
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@SpringBootApplication
@ConfigurationPropertiesScan
class Launcher(
	registerService: CommandRegisterService,
	commands: List<InteractionCommand>,
	collectorRegistry: CollectorRegistry,
	gatewayService: GatewayService,
	restService: DiscordEntityRestService,
	properties: DiscordProperties,
	environment: Environment,
) {

	init {
		// https://github.com/prometheus/client_java/issues/279
		collectorRegistry.clear()

		Thread.setDefaultUncaughtExceptionHandler { t, e -> logger().warn("Uncaught exception in thread {}", t.name, e) }
		DecoroutinatorRuntime.load()

		logger().info("Henlo")

		if (environment.acceptsProfiles(Profiles.of("test"))) {
			logger().info("Test profile active, not launching shards")
		} else {
			val gatewayBot = runBlocking {
				registerService.bulkOverwrite(commands.map { it.creation() })

				restService.gateway.getGatewayBot()
			}

			val shardsAmount = gatewayBot.shards  //+ 10 //TODO remove

			logger().info("Shards: {}", shardsAmount)

			val shards = (0 until shardsAmount).toSet()

			val gatewayProps = GatewayProps(
				URI.create(gatewayBot.url), // TODO figure out caching rules for this thing
				properties.botToken,
				IntBitfield.of(*GatewayIntent.values()),
				shardsAmount,
			)
			logger().info("Launching shards")
			gatewayService.launch(shards, gatewayProps)
			logger().info("Shards launched")
		}
	}
}

fun main(args: Array<String>) {
	System.setProperty("spring.config.name", "shipa")
	val app = SpringApplication(Launcher::class.java)
	app.setAdditionalProfiles("secrets")
	app.run(*args)
}
