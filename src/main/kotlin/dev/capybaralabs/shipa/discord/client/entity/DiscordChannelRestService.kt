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
import java.util.Optional
import org.springframework.http.RequestEntity
import org.springframework.web.util.UriComponentsBuilder

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


	// https://discord.com/developers/docs/resources/channel#get-channel-messages
	suspend fun fetchMessages(channelId: Long, around: Long? = null, before: Long? = null, after: Long? = null, limit: Int? = null): List<Message> {
		val uriBuilder = UriComponentsBuilder
			.fromUriString("/channels/{channelId}/messages")

		around?.let { uriBuilder.queryParam("around", it) }
		before?.let { uriBuilder.queryParam("before", it) }
		after?.let { uriBuilder.queryParam("after", it) }
		limit?.let { uriBuilder.queryParam("limit", it) }

		return discordRestService.exchange<List<Message>>(
			"$applicationId-$channelId",
			RequestEntity
				.get(uriBuilder.buildAndExpand(channelId).toUriString())
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/channel#get-channel-message
	suspend fun fetchMessage(channelId: Long, messageId: Long): Message {
		return discordRestService.exchange<Message>(
			"$applicationId-$channelId",
			RequestEntity
				.get("/channels/{channelId}/messages/{messageId}", channelId, messageId)
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/channel#edit-message
	suspend fun editMessage(channelId: Long, messageId: Long, editRequest: EditMessage): Message {
		return discordRestService.exchange<Message>(
			"$applicationId-$channelId",
			RequestEntity
				.patch("/channels/{channelId}/messages/{messageId}", channelId, messageId)
				.body(editRequest)
		).body!!
	}

	// set values to null to delete them on the original message
	data class EditMessage(
		val content: Optional<String>? = Optional.empty(),
		val embeds: Optional<List<Embed>>? = Optional.empty(),
		val flags: Optional<IntBitfield<MessageFlag>>? = Optional.empty(), // SUPPRESS_EMBEDS only
		val allowedMentions: Optional<AllowedMentions>? = Optional.of(AllowedMentions.none()),
		val components: Optional<List<ActionRow>>? = Optional.empty(),
//		val files[n]: List<???>? = null,
//		val payloadJson: String? = null,
//		val attachments: List<PartialAttachment>? = null,
	)


}