package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.interaction.model.create.Command
import java.util.concurrent.ConcurrentHashMap
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * Simple command lookup implementation that will work fine with dynamic customId registrations as long as running on a single instance
 */
@Service
class InMemoryCommandLookupService(
	commands: List<Command>,
) : CommandLookupService {

	private val byName: MutableMap<String, Command> = HashMap()
	private val byStaticCustomId: MutableMap<String, Command> = HashMap()
	private val byDynamicCustomId: MutableMap<String, Command> = ConcurrentHashMap() // TODO implement expiration

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
		byDynamicCustomId[event.customId] = event.command // TODO check for overwrites?
	}

	override fun findByName(name: String): Command? {
		return byName[name]
	}

	override fun findByCustomId(customId: String): Command? {
		return byStaticCustomId[customId]
	}
}

class RegisterDynamicCustomId(
	val customId: String,
	val command: Command,
)
