package dev.capybaralabs.shipa.discord.interaction.model

/**
 * [Discord Interaction Response](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-interaction-response-structure)
 */
data class InteractionResponse(val type: InteractionCallbackType, val data: InteractionCallbackData? = null)
