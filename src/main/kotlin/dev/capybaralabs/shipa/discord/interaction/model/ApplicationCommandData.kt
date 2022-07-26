package dev.capybaralabs.shipa.discord.interaction.model

/**
 * [Discord Application Command Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-application-command-data-structure)
 */
data class ApplicationCommandData(
	val id: Long,
	val name: String,
	val type: ApplicationCommandType,
	val resolved: ResolvedData?,
//	val options: List<ApplicationCommandInteractionDataOption>?,
	val guildId: Long?,
	val targetId: Long?,
) : InteractionData
