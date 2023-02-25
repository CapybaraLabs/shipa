package dev.capybaralabs.shipa.discord

import java.time.Instant

/**
 * https://discord.com/developers/docs/reference#snowflakes
 */
const val DISCORD_EPOCH = 1420070400000L
fun Instant.asDiscordSnowflake(): Long {
	return (toEpochMilli() - DISCORD_EPOCH) shl 22
}
