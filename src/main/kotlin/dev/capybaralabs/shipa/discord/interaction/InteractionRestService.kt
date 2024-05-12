package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.client.MultipartBody
import dev.capybaralabs.shipa.discord.client.ratelimit.WebhooksIdToken
import dev.capybaralabs.shipa.discord.client.ratelimit.WebhooksIdTokenMessagesId
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallback
import dev.capybaralabs.shipa.discord.model.Message
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service

/**
 * REST calls to the Discord Interactions API
 *
 * [Discord Interaction Endpoints](https://discord.com/developers/docs/interactions/receiving-and-responding#endpoints)
 */
@Service
class InteractionRestService(
	properties: DiscordProperties,
	private val discordRestService: DiscordRestService,
) {

	private val retryNotFoundTimes = 3
	private val applicationId = properties.applicationId

	// https://discord.com/developers/docs/interactions/receiving-and-responding#get-original-interaction-response
	suspend fun fetchOriginalResponse(token: String): Message {
		return discordRestService.exchange<Message>(
			WebhooksIdTokenMessagesId(applicationId, token),
			RequestEntity
				.get("/webhooks/{applicationId}/{token}/messages/@original", applicationId, token)
				.build(),
			retryNotFoundTimes = retryNotFoundTimes,
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#edit-original-interaction-response
	suspend fun editOriginalResponse(token: String, data: InteractionCallback.FollowupMessage): Message {

		val requestBuilder = RequestEntity.patch("/webhooks/{applicationId}/{token}/messages/@original", applicationId, token)

		val potentialMultipartBody = data.toPotentialMultipartBody()
		if (potentialMultipartBody is MultipartBody) {
			requestBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
		}

		return discordRestService.exchange<Message>(
			WebhooksIdTokenMessagesId(applicationId, token),
			requestBuilder.body(potentialMultipartBody.body),
			retryNotFoundTimes = retryNotFoundTimes,
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#delete-original-interaction-response
	suspend fun deleteOriginalResponse(token: String) {
		discordRestService.exchange<Void>(
			WebhooksIdTokenMessagesId(applicationId, token),
			RequestEntity
				.delete("/webhooks/{applicationId}/{token}/messages/@original", applicationId, token)
				.build(),
			retryNotFoundTimes = retryNotFoundTimes,
		)
	}


	// https://discord.com/developers/docs/interactions/receiving-and-responding#create-followup-message
	suspend fun createFollowupMessage(token: String, data: InteractionCallback.FollowupMessage): Message {

		val requestBuilder = RequestEntity.post("/webhooks/{applicationId}/{token}", applicationId, token)

		val multipartBody = data.toPotentialMultipartBody()
		if (multipartBody is MultipartBody) {
			requestBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
		}

		return discordRestService.exchange<Message>(
			WebhooksIdToken(applicationId, token),
			requestBuilder.body(multipartBody.body),
			retryNotFoundTimes = retryNotFoundTimes,
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#get-followup-message
	suspend fun fetchFollowupMessage(token: String, messageId: Long): Message {
		return discordRestService.exchange<Message>(
			WebhooksIdTokenMessagesId(applicationId, token),
			RequestEntity
				.get("/webhooks/{applicationId}/{token}/messages/{messageId}", applicationId, token, messageId)
				.build(),
			retryNotFoundTimes = retryNotFoundTimes,
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#edit-followup-message
	suspend fun editFollowupMessage(token: String, data: InteractionCallback.FollowupMessage, messageId: Long): Message {

		val requestBuilder = RequestEntity.patch("/webhooks/{applicationId}/{token}/messages/{messageId}", applicationId, token, messageId)

		val multipartBody = data.toPotentialMultipartBody()
		if (multipartBody is MultipartBody) {
			requestBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
		}

		return discordRestService.exchange<Message>(
			WebhooksIdTokenMessagesId(applicationId, token),
			requestBuilder.body(multipartBody.body),
			retryNotFoundTimes = retryNotFoundTimes,
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#delete-followup-message
	suspend fun deleteFollowupMessage(token: String, messageId: Long) {
		discordRestService.exchange<Void>(
			WebhooksIdTokenMessagesId(applicationId, token),
			RequestEntity
				.delete("/webhooks/{applicationId}/{token}/messages/{messageId}", applicationId, token, messageId)
				.build(),
			retryNotFoundTimes = retryNotFoundTimes,
		)
	}
}
