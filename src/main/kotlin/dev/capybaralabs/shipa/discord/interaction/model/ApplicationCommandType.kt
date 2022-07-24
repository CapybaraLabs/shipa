package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * [Discord Application Command Type](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-types)
 */
enum class ApplicationCommandType(@JsonValue val value: Int) {
	CHAT_INPUT(1),
	USER(2),
	MESSAGE(3),
}
