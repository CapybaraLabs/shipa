package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.model.DefaultMessageNotificationLevel
import dev.capybaralabs.shipa.discord.model.DiscordLocale
import dev.capybaralabs.shipa.discord.model.ExplicitContentFilterLevel
import dev.capybaralabs.shipa.discord.model.Guild
import dev.capybaralabs.shipa.discord.model.GuildPreview
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.Member
import dev.capybaralabs.shipa.discord.model.SystemChannelFlag
import dev.capybaralabs.shipa.discord.model.VerificationLevel
import java.util.Optional
import org.springframework.http.RequestEntity
import org.springframework.web.util.UriComponentsBuilder

/**
 * Rest Client for the [Discord Guild Resource](https://discord.com/developers/docs/resources/guild)
 */
class DiscordGuildRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

	// https://discord.com/developers/docs/resources/guild#modify-guild
	suspend fun modifyGuild(guildId: Long, reason: String? = null, modifyRequest: ModifyGuild): Guild {
		val builder = RequestEntity.patch("/guilds/{guildId}", guildId)
		reason?.let { builder.header("X-Audit-Log-Reason", it) }


		return discordRestService.exchange<Guild>(
			"$applicationId-$guildId",
			builder.body(modifyRequest),
		).body!!
	}

	data class ModifyGuild(
		val name: String? = null,
		val verificationLevel: Optional<VerificationLevel>? = null,
		val defaultMessageNotifications: Optional<DefaultMessageNotificationLevel>? = null,
		val explicitContentFilter: Optional<ExplicitContentFilterLevel>? = null,
		val afkChannelId: Optional<Long>? = null,
		val afkTimeout: Long? = null, // seconds
		val icon: Optional<String>? = null,
		val ownerId: Long? = null,
		val splash: Optional<String>? = null,
		val discoverySplash: Optional<String>? = null,
		val banner: Optional<String>? = null,
		val systemChannelId: Optional<Long>? = null,
		val systemChannelFlags: IntBitfield<SystemChannelFlag>? = null,
		val rulesChannelId: Optional<Long>? = null,
		val publicUpdatesChannelId: Optional<Long>? = null,
		val preferredLocale: Optional<DiscordLocale>? = null,
		val features: List<String>? = null,
		val description: Optional<String>? = null,
		val premiumProgressBarEnabled: Boolean? = null,
	)

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
