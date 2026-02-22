package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.ShipaDiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.client.FileUpload
import dev.capybaralabs.shipa.discord.client.MultipartBody
import dev.capybaralabs.shipa.discord.client.PayloadWithFiles
import dev.capybaralabs.shipa.discord.client.WithAttachments
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
import dev.capybaralabs.shipa.discord.model.PartialAttachment
import dev.capybaralabs.shipa.discord.namedQueryParam
import java.util.Optional
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.web.util.UriComponentsBuilder

/**
 * Rest Client for the [Discord Channels Resource](https://discord.com/developers/docs/resources/channel)
 */
class DiscordChannelRestService(
	properties: ShipaDiscordProperties,
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
	suspend fun createMessage(channelId: Long, createMessageRequest: CreateMessageRequest): Message {

		val requestBuilder = RequestEntity.post("/channels/{channelId}/messages", channelId)

		val multipartBody = createMessageRequest.toPotentialMultipartBody()
		if (multipartBody is MultipartBody) {
			requestBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
		}
		return discordRestService.exchange<Message>(
			ChannelsIdMessages(channelId),
			requestBuilder.body(multipartBody.body),
		).body!!
	}

	data class CreateMessageRequest(
		val createMessage: CreateMessage,
		override val files: List<FileUpload>? = null,
	) : PayloadWithFiles<CreateMessage> {
		override val payload = createMessage
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
		override val attachments: List<PartialAttachment>? = null,
		val flags: IntBitfield<MessageFlag>? = null, // SUPPRESS_EMBEDS only
	) : WithAttachments<CreateMessage> {

		override fun copyWithAttachments(attachments: List<PartialAttachment>): CreateMessage {
			return copy(attachments = attachments)
		}
	}


	// https://discord.com/developers/docs/resources/channel#get-channel-messages
	suspend fun fetchMessages(channelId: Long, around: Long? = null, before: Long? = null, after: Long? = null, limit: Int? = null): List<Message> {
		val uriTemplate = "/channels/{channelId}/messages"
		val uriBuilder = UriComponentsBuilder
			.fromUriString(uriTemplate)
		val uriVariables = mutableMapOf<String, Any>("channelId" to channelId)

		around?.let {
			uriBuilder.namedQueryParam("around")
			uriVariables.put("around", it)
		}
		before?.let {
			uriBuilder.namedQueryParam("before")
			uriVariables.put("before", it)
		}
		after?.let {
			uriBuilder.namedQueryParam("after")
			uriVariables.put("after", it)
		}
		limit?.let {
			uriBuilder.namedQueryParam("limit")
			uriVariables.put("limit", it)
		}

		return discordRestService.exchange<List<Message>>(
			ChannelsIdMessages(channelId),
			RequestEntity.method(
				HttpMethod.GET,
				uriBuilder.build().toUriString(),
				uriVariables,
			).build(),
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
