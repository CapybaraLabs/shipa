package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.client.ratelimit.UsersId
import dev.capybaralabs.shipa.discord.client.ratelimit.UsersIdChannels
import dev.capybaralabs.shipa.discord.client.ratelimit.UsersIdGuilds
import dev.capybaralabs.shipa.discord.model.Channel
import dev.capybaralabs.shipa.discord.model.PartialGuild
import dev.capybaralabs.shipa.discord.model.User
import dev.capybaralabs.shipa.discord.oauth2.OAuth2Scope.GUILDS
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
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
			UsersId,
			RequestEntity
				.get("/users/@me")
				.build(),
		).body!!
	}

	// https://discord.com/developers/docs/resources/user#get-user
	suspend fun fetchUser(userId: Long): User {
		return discordRestService.exchange<User>(
			UsersId,
			RequestEntity
				.get("/users/{userId}", userId)
				.build(),
		).body!!
	}


	// https://discord.com/developers/docs/resources/user#create-dm
	suspend fun createDm(recipientId: Long): Channel {
		return discordRestService.exchange<Channel>(
			UsersIdChannels,
			RequestEntity
				.post("/users/@me/channels")
				.body(mapOf("recipient_id" to recipientId)),
		).body!!
	}

	// https://discord.com/developers/docs/resources/user#modify-current-user
	suspend fun modifyCurrentUser(username: String? = null, avatar: Optional<String>? = null): User {
		val request = mutableMapOf<String, String?>()
		username?.let { request.put("username", it) }
		avatar?.let { request.put("avatar", it.getOrNull()) }

		return discordRestService.exchange<User>(
			UsersId,
			RequestEntity
				.patch("/users/@me")
				.body(request),
		).body!!
	}

	// https://discord.com/developers/docs/resources/user#get-current-user-guilds
	suspend fun listCurrentUserGuilds(): List<PartialGuild> {
		discordRestService.assertUserHasScope(GUILDS)

		return discordRestService.exchange<List<PartialGuild>>(
			UsersIdGuilds,
			RequestEntity
				.get("/users/@me/guilds")
				.build(),
		).body!!
	}

}
