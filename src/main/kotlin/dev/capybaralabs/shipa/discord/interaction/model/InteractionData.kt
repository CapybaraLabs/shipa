package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.DEDUCTION

/**
 * [Discord Interaction Data](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object-interaction-data)
 */
@JsonTypeInfo(use = DEDUCTION)
sealed interface InteractionData
