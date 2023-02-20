package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.DiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.client.ratelimit.InvitesCode
import dev.capybaralabs.shipa.discord.model.Invite
import org.springframework.http.RequestEntity


/**
 * Rest Client for the [Discord Invite Resource](https://discord.com/developers/docs/resources/invite)
 */
class DiscordInviteRestService(
	properties: DiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

	// https://discord.com/developers/docs/resources/invite#get-invite
	suspend fun fetchInvite(inviteCode: String): Invite {
		return discordRestService.exchange<Invite>(
			InvitesCode,
			RequestEntity
				.get("/invites/{inviteCode}", inviteCode)
				.build(),
		).body!!
	}
}
