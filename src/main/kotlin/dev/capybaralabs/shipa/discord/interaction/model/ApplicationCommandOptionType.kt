package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * [Discord Application Command Option Type](https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-option-type)
 */
enum class ApplicationCommandOptionType(@JsonValue val value: Int) {
	SUB_COMMAND(1),
	SUB_COMMAND_GROUP(2),
	STRING(3),
	INTEGER(4),
	BOOLEAN(5),
	USER(6),
	CHANNEL(7),
	ROLE(8),
	MENTIONABLE(9),
	NUMBER(10),
	ATTACHMENT(11),
}
