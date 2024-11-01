package dev.capybaralabs.shipa.discord

import io.micrometer.core.instrument.Timer
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

fun <T> Timer.time(block: () -> T): T {
	return Timer.start().let {
		try {
			block.invoke()
		} finally {
			it.stop(this)
		}
	}
}

suspend fun <T> Timer.timeSuspending(block: suspend () -> T): T {
	return Timer.start().let {
		try {
			block.invoke()
		} finally {
			it.stop(this)
		}
	}
}
