package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.RestService
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData
import dev.capybaralabs.shipa.discord.model.Message
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
	private val restService: RestService,
) {

	private val applicationId = properties.applicationId

	// https://discord.com/developers/docs/interactions/receiving-and-responding#get-original-interaction-response
	suspend fun getOriginalResponse(token: String): Message {
		return restService.exchange<Message>(
			"$applicationId-$token",
			RequestEntity
				.get("/webhooks/{applicationId}/{token}/messages/@original", applicationId, token)
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#edit-original-interaction-response
	suspend fun editOriginalResponse(token: String, data: InteractionCallbackData): Message {
		return restService.exchange<Message>(
			"$applicationId-$token",
			RequestEntity
				.patch("/webhooks/{applicationId}/{token}/messages/@original", applicationId, token)
				.body(data)
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#delete-original-interaction-response
	suspend fun deleteOriginalResponse(token: String) {
		restService.exchange<Void>(
			"$applicationId-$token",
			RequestEntity
				.delete("/webhooks/{applicationId}/{token}/messages/@original", applicationId, token)
				.build()
		)
	}


	// https://discord.com/developers/docs/interactions/receiving-and-responding#create-followup-message
	suspend fun createFollowupMessage(token: String, data: InteractionCallbackData): Message {
		return restService.exchange<Message>(
			"$applicationId-$token",
			RequestEntity
				.post("/webhooks/{applicationId}/{token}", applicationId, token)
				.body(data)
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#get-followup-message
	suspend fun getFollowupMessage(token: String, messageId: Long): Message {
		return restService.exchange<Message>(
			"$applicationId-$token",
			RequestEntity
				.get("/webhooks/{applicationId}/{token}/messages/{messageId}", applicationId, token, messageId)
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#edit-followup-message
	suspend fun editFollowupMessage(token: String, response: InteractionCallbackData, messageId: Long): Message {
		return restService.exchange<Message>(
			"$applicationId-$token",
			RequestEntity
				.patch("/webhooks/{applicationId}/{token}/messages/{messageId}", applicationId, token, messageId)
				.body(response)
		).body!!
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#delete-followup-message
	suspend fun deleteFollowupMessage(token: String, messageId: Long) {
		restService.exchange<Void>(
			"$applicationId-$token",
			RequestEntity
				.delete("/webhooks/{applicationId}/{token}/messages/{messageId}", applicationId, token, messageId)
				.build()
		)
	}

}
