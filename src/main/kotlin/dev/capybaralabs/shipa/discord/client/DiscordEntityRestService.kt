package dev.capybaralabs.shipa.discord.client

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.model.Channel
import dev.capybaralabs.shipa.discord.model.Guild
import dev.capybaralabs.shipa.discord.model.GuildPreview
import dev.capybaralabs.shipa.discord.model.Member
import dev.capybaralabs.shipa.discord.model.User
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class DiscordEntityRestService(
	properties: DiscordProperties,
	private val discordRestService: DiscordRestService,
) {
	private val applicationId = properties.applicationId


	// https://discord.com/developers/docs/resources/channel#get-channel
	suspend fun fetchChannel(channelId: Long): Channel {
		return discordRestService.exchange<Channel>(
			"$applicationId-$channelId",
			RequestEntity
				.get("/channels/{channelId}", channelId)
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/guild#get-guild
	suspend fun fetchGuild(guildId: Long, withCounts: Boolean? = null): Guild {
		val uriBuilder = UriComponentsBuilder
			.fromUriString("/guilds/{guildId}")
		withCounts?.let { uriBuilder.queryParam("with_counts", it) }

		return discordRestService.exchange<Guild>(
			"$applicationId-$guildId",
			RequestEntity
				.get(uriBuilder.buildAndExpand(guildId).toUriString())
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/guild#get-guild-preview
	suspend fun fetchGuildPreview(guildId: Long): GuildPreview {
		return discordRestService.exchange<GuildPreview>(
			"$applicationId-$guildId",
			RequestEntity
				.get("/guilds/{guildId}/preview", guildId)
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/guild#get-guild-member
	suspend fun fetchGuildMember(guildId: Long, userId: Long): Member {
		return discordRestService.exchange<Member>(
			"$applicationId-$guildId",
			RequestEntity
				.get("/guilds/{guildId}/members/{userId}", guildId, userId)
				.build()
		).body!!
	}

	// https://discord.com/developers/docs/resources/guild#list-guild-members
	suspend fun fetchGuildMembers(guildId: Long, limit: Int? = null, after: Long? = null): List<Member> {
		val uriBuilder = UriComponentsBuilder
			.fromUriString("/guilds/{guildId}/members")

		limit?.let { uriBuilder.queryParam("limit", it) }
		after?.let { uriBuilder.queryParam("after", it) }

		return discordRestService.exchange<List<Member>>(
			"$applicationId-$guildId",
			RequestEntity
				.get(uriBuilder.buildAndExpand(guildId).toUriString())
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


}
