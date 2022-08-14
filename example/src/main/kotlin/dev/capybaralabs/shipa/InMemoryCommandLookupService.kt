package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.interaction.command.CommandLookupService
import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import java.util.concurrent.ConcurrentHashMap
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * Simple command lookup implementation that will work fine with dynamic customId registrations as long as running on a single instance
 */
@Service
class InMemoryCommandLookupService(
	commands: List<InteractionCommand>,
) : CommandLookupService {

	private val byName: MutableMap<String, InteractionCommand> = HashMap()
	private val byStaticCustomId: MutableMap<String, InteractionCommand> = HashMap()
	private val byDynamicCustomId: MutableMap<String, InteractionCommand> = ConcurrentHashMap()

	init {
		commands.forEach { command ->
			byName[command.name()] = command
			command.staticCustomIds().forEach { customId ->
				byStaticCustomId[customId] = command
			}
		}
	}

	@EventListener
	fun onRegisterDynamicCustomId(event: RegisterDynamicCustomId) {
		byDynamicCustomId[event.customId] = event.command
	}

	override fun findByName(name: String): InteractionCommand? {
		return byName[name]
	}

	override fun findByCustomId(customId: String): InteractionCommand? {
		return byStaticCustomId[customId]
	}
}

class RegisterDynamicCustomId(
	val customId: String,
	val command: InteractionCommand,
)
