package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * [Discord Interaction Types](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-interaction-type)
 */
enum class InteractionType(@JsonValue val value: Int) {
	PING(1),
	APPLICATION_COMMAND(2),
	MESSAGE_COMPONENT(3),
	APPLICATION_COMMAND_AUTOCOMPLETE(4),
	MODAL_SUBMIT(5),
	;

	companion object {
		fun fromValue(value: Int) = values().first { it.value == value }
	}
}
