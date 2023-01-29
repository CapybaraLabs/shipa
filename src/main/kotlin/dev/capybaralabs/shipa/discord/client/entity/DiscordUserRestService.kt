package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.model.Channel
import dev.capybaralabs.shipa.discord.model.User
import org.springframework.http.RequestEntity


/**
 * Rest Client for the [Discord Users Resource](https://discord.com/developers/docs/resources/user)
 */
class DiscordUserRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

	// https://discord.com/developers/docs/resources/user#get-current-user
	suspend fun fetchSelf(): User {
		return discordRestService.exchange<User>(
			"$applicationId",
			RequestEntity
				.get("/users/@me")
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/user#get-user
	suspend fun fetchUser(userId: Long): User {
		return discordRestService.exchange<User>(
			"$applicationId",
			RequestEntity
				.get("/users/{userId}", userId)
				.build()
		).body!!
	}


	// https://discord.com/developers/docs/resources/user#create-dm
	suspend fun createDm(recipientId: Long): Channel {
		return discordRestService.exchange<Channel>(
			"$applicationId",
			RequestEntity
				.post("/users/@me/channels")
				.body(mapOf("recipient_id" to recipientId))
		).body!!
	}


}
