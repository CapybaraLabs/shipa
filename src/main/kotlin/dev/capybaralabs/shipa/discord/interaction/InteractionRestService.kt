package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.interaction.model.InteractionCallbackData
import dev.capybaralabs.shipa.discord.model.Message
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.patchForObject
import org.springframework.web.client.postForEntity
import org.springframework.web.client.postForObject

/**
 * REST calls to the Discord Interactions API
 *
 * [Discord Interaction Endpoints](https://discord.com/developers/docs/interactions/receiving-and-responding#endpoints)
 */
@Service
class InteractionRestService(
	private val properties: DiscordProperties,
	private val restTemplate: RestTemplate,
) {

	// https://discord.com/developers/docs/interactions/receiving-and-responding#create-interaction-response
	fun createResponse(token: String, interactionId: Long, response: InteractionCallbackData) {
		restTemplate.postForEntity<Void>(
			"/interactions/{interactionId}/{token}/callback",
			response, interactionId, token
		)
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#get-original-interaction-response
	fun getOriginalResponse(token: String): Message {
		return restTemplate.getForObject(
			"/webhooks/{applicationId}/{token}/messages/@original",
			properties.applicationId, token,
		)
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#edit-original-interaction-response
	fun editOriginalResponse(token: String, response: InteractionCallbackData): Message {
		return restTemplate.patchForObject(
			"/webhooks/{applicationId}/{token}/messages/@original",
			response, properties.applicationId, token
		)
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#delete-original-interaction-response
	fun deleteOriginalResponse(token: String) {
		restTemplate.delete(
			"/webhooks/{applicationId}/{token}/messages/@original",
			properties.applicationId, token,
		)
	}


	// https://discord.com/developers/docs/interactions/receiving-and-responding#create-followup-message
	fun createFollowupMessage(token: String, response: InteractionCallbackData): Message {
		return restTemplate.postForObject(
			"/webhooks/{applicationId}/{token}",
			response, properties.applicationId, token
		)
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#get-followup-message
	fun getFollowupMessage(token: String, messageId: Long): Message {
		return restTemplate.getForObject(
			"/webhooks/{applicationId}/{token}/messages/{messageId}",
			properties.applicationId, token, messageId
		)
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#edit-followup-message
	fun editFollowupMessage(token: String, response: InteractionCallbackData, messageId: Long): Message {
		return restTemplate.patchForObject(
			"/webhooks/{applicationId}/{token}/messages/{messageId}",
			response, properties.applicationId, token, messageId
		)
	}

	// https://discord.com/developers/docs/interactions/receiving-and-responding#delete-followup-message
	fun deleteFollowupMessage(token: String, messageId: Long) {
		restTemplate.delete(
			"/webhooks/{applicationId}/{token}/messages/{messageId}",
			properties.applicationId, token, messageId
		)
	}

}
