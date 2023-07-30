package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * [Discord Interaction Callback Types](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-interaction-callback-type)
 */
enum class InteractionCallbackType(@JsonValue val value: Int) {
	PONG(1),
	CHANNEL_MESSAGE_WITH_SOURCE(4),
	DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),
	DEFERRED_UPDATE_MESSAGE(6),
	UPDATE_MESSAGE(7),
	APPLICATION_COMMAND_AUTOCOMPLETE_RESULT(8),
	MODAL(9),
	;

	companion object {
		fun fromValue(value: Int) = entries.first { it.value == value }
	}
}
