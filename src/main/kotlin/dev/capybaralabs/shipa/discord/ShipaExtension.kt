package dev.capybaralabs.shipa.discord

import java.time.Instant
import org.springframework.web.util.UriComponentsBuilder

/**
 * https://discord.com/developers/docs/reference#snowflakes
 */
const val DISCORD_EPOCH = 1420070400000L
fun Instant.asDiscordSnowflake(): Long {
	return (toEpochMilli() - DISCORD_EPOCH) shl 22
}

fun UriComponentsBuilder.namedQueryParam(name: String): UriComponentsBuilder {
	return queryParam(name, "{$name}")
}
