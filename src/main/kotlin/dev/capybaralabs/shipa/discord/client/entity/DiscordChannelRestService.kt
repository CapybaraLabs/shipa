package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ActionRow
import dev.capybaralabs.shipa.discord.model.AllowedMentions
import dev.capybaralabs.shipa.discord.model.Channel
import dev.capybaralabs.shipa.discord.model.Embed
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.Message
import dev.capybaralabs.shipa.discord.model.MessageFlag
import org.springframework.http.RequestEntity

/**
 * Rest Client for the [Discord Channels Resource](https://discord.com/developers/docs/resources/channel)
 */
class DiscordChannelRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

	// https://discord.com/developers/docs/resources/channel#get-channel
	suspend fun fetchChannel(channelId: Long): Channel {
		return discordRestService.exchange<Channel>(
			"$applicationId-$channelId",
			RequestEntity
				.get("/channels/{channelId}", channelId)
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/channel#create-message
	suspend fun createMessage(channelId: Long, createMessage: CreateMessage): Message {
		return discordRestService.exchange<Message>(
			"$applicationId-$channelId",
			RequestEntity
				.post("/channels/{channelId}/messages", channelId)
				.body(createMessage)
		).body!!
	}

	data class CreateMessage(
		val content: String? = null,
		val nonce: Int? = null,
		val tts: Boolean? = null,
		val embeds: List<Embed>? = null,
		val allowedMentions: AllowedMentions? = AllowedMentions.none(),
//		val messageReference: MessageReference? = null,
		val components: List<ActionRow>? = null,
		val stickerIds: List<Long>? = null,
//		val files[n]: List<???>? = null,
//		val payloadJson: String? = null,
//		val attachments: List<PartialAttachment>? = null,
		val flags: IntBitfield<MessageFlag>? = null, // SUPPRESS_EMBEDS only
	)
}
