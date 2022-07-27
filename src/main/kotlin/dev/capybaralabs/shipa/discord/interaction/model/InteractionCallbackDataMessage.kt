package dev.capybaralabs.shipa.discord.interaction.model

import dev.capybaralabs.shipa.discord.model.AllowedMentions
import dev.capybaralabs.shipa.discord.model.Embed

/**
 * [Discord Interaction Message Callback](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-response-object-messages)
 */
data class InteractionCallbackDataMessage(
	val tts: Boolean? = null,
	val content: String? = null,
	val embeds: List<Embed>? = null,
	val allowedMentions: AllowedMentions? = null,
	val flags: Int? = null,
//	val components: List<Component>?,
//	val attachments: List<PartialAttachment>?,
) : InteractionCallbackData
