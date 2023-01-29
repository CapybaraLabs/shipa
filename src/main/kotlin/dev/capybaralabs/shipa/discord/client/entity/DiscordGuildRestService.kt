package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.model.Guild
import dev.capybaralabs.shipa.discord.model.GuildPreview
import dev.capybaralabs.shipa.discord.model.Member
import org.springframework.http.RequestEntity
import org.springframework.web.util.UriComponentsBuilder

/**
 * Rest Client for the [Discord Guild Resource](https://discord.com/developers/docs/resources/guild)
 */
class DiscordGuildRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

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
	suspend fun listGuildMembers(guildId: Long, limit: Int? = null, after: Long? = null): List<Member> {
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

	// https://discord.com/developers/docs/resources/guild#remove-guild-member
	suspend fun removeGuildMember(guildId: Long, userId: Long, reason: String? = null) {
		val builder = RequestEntity.delete("/guilds/{guildId}/members/{userId}", guildId, userId)
		reason?.let { builder.header("X-Audit-Log-Reason", it) }

		discordRestService.exchange<Unit>(
			"$applicationId-$guildId",
			builder.build()
		)
	}

	// https://discord.com/developers/docs/resources/guild#create-guild-ban
	suspend fun createBan(guildId: Long, userId: Long, reason: String? = null, deleteMessageSeconds: Int? = null) {
		val builder = RequestEntity.put("/guilds/{guildId}/bans/{userId}", guildId, userId)
		reason?.let { builder.header("X-Audit-Log-Reason", it) }

		val request = deleteMessageSeconds?.let {
			builder.body(mapOf("delete_message_seconds" to deleteMessageSeconds))
		} ?: builder.build()

		discordRestService.exchange<Unit>(
			"$applicationId-$guildId",
			request
		)
	}

}
