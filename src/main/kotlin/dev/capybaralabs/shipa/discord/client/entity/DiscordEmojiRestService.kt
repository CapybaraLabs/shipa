package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.ShipaDiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import dev.capybaralabs.shipa.discord.client.ratelimit.GuildsIdEmojis
import dev.capybaralabs.shipa.discord.model.Emoji
import org.springframework.http.RequestEntity


/**
 * Rest Client for the [Discord Emoji Resource](https://discord.com/developers/docs/resources/emoji)
 */
class DiscordEmojiRestService(
	properties: ShipaDiscordProperties,
	discordRestService: DiscordRestService,
) : BaseDiscordEntityRestService(properties, discordRestService) {

	// https://discord.com/developers/docs/resources/emoji#list-guild-emojis
	suspend fun listGuildEmojis(guildId: Long): List<Emoji> {
		return discordRestService.exchange<List<Emoji>>(
			GuildsIdEmojis(guildId),
			RequestEntity
				.get("/guilds/{guildId}/emojis", guildId)
				.build(),
		).body!!
	}
}
