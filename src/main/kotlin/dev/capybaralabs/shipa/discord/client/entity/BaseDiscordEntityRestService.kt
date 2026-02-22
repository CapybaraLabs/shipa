package dev.capybaralabs.shipa.discord.client.entity

import dev.capybaralabs.shipa.discord.ShipaDiscordProperties
import dev.capybaralabs.shipa.discord.client.DiscordRestService
import java.text.Normalizer


internal fun String.toAscii(): String {
	val normalized = Normalizer.normalize(this, Normalizer.Form.NFKD)
	return normalized.replace("[^\\p{ASCII}]".toRegex(), "")
}

abstract class BaseDiscordEntityRestService(
	properties: ShipaDiscordProperties,
	protected val discordRestService: DiscordRestService,
) {
	protected val applicationId = properties.applicationId

}
