package dev.capybaralabs.shipa.discord.interaction.model

/**
 * [Discord Interaction](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object)
 */
data class InteractionObject(val id: Long, val applicationId: Long, val type: InteractionType)