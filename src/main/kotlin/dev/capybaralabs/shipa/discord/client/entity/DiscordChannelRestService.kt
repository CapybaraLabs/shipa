package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.client.ratelimit.ChannelsId
import dev.capybaralabs.shipa.discord.client.ratelimit.ChannelsIdInvites
import dev.capybaralabs.shipa.discord.client.ratelimit.ChannelsIdMessages
import dev.capybaralabs.shipa.discord.client.ratelimit.ChannelsIdMessagesId
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ActionRow
import dev.capybaralabs.shipa.discord.model.AllowedMentions
import dev.capybaralabs.shipa.discord.model.Channel
import dev.capybaralabs.shipa.discord.model.Embed
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.Invite
import dev.capybaralabs.shipa.discord.model.Message
import dev.capybaralabs.shipa.discord.model.MessageFlag
import java.util.Optional
import org.springframework.http.HttpMethod
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
			ChannelsId(channelId),
			RequestEntity
				.get("/channels/{channelId}", channelId)
				.build(),
		).body!!
	}

	// https://discord.com/developers/docs/resources/channel#create-message
	suspend fun createMessage(channelId: Long, createMessage: CreateMessage): Message {
		return discordRestService.exchange<Message>(
			ChannelsIdMessages(channelId),
			RequestEntity
				.post("/channels/{channelId}/messages", channelId)
				.body(createMessage),
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
		val uriTemplate = "/channels/{channelId}/messages"
		val uriBuilder = UriComponentsBuilder
			.fromUriString(uriTemplate)

		around?.let { uriBuilder.queryParam("around", it) }
		before?.let { uriBuilder.queryParam("before", it) }
		after?.let { uriBuilder.queryParam("after", it) }
		limit?.let { uriBuilder.queryParam("limit", it) }

		return discordRestService.exchange<List<Message>>(
			ChannelsIdMessages(channelId),
			RequestEntity
				.get(uriBuilder.buildAndExpand(channelId).toUriString())
				.build(),
			uriTemplate,
		).body!!
	}

	// https://discord.com/developers/docs/resources/channel#get-channel-message
	suspend fun fetchMessage(channelId: Long, messageId: Long): Message {
		return discordRestService.exchange<Message>(
			ChannelsIdMessagesId(HttpMethod.GET, channelId),
			RequestEntity
				.get("/channels/{channelId}/messages/{messageId}", channelId, messageId)
				.build(),
		).body!!
	}

	// https://discord.com/developers/docs/resources/channel#edit-message
	suspend fun editMessage(channelId: Long, messageId: Long, editRequest: EditMessage): Message {
		return discordRestService.exchange<Message>(
			ChannelsIdMessagesId(HttpMethod.PATCH, channelId),
			RequestEntity
				.patch("/channels/{channelId}/messages/{messageId}", channelId, messageId)
				.body(editRequest),
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


	// https://discord.com/developers/docs/resources/channel#create-channel-invite
	suspend fun createInvite(channelId: Long, reason: String? = null, createRequest: CreateInvite? = null): Invite {
		val builder = RequestEntity.post("/channels/{channelId}/invites", channelId)
		reason?.let { builder.header("X-Audit-Log-Reason", it.toAscii()) }

		return discordRestService.exchange<Invite>(
			ChannelsIdInvites(channelId),
			builder.body(createRequest ?: CreateInvite()),
		).body!!
	}

	data class CreateInvite(
		val maxAge: Int? = null,
		val maxUses: Int? = null,
		val temporary: Boolean? = null,
		val unique: Boolean? = null,
		val targetType: Int? = null,
		val targetUserId: Long? = null,
		val targetApplicationId: Long? = null,
	)

}
