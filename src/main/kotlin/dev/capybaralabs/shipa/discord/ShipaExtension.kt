package dev.capybaralabs.shipa.discord

import io.micrometer.core.instrument.Timer
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration
import org.springframework.web.util.UriComponentsBuilder

/**
 * https://discord.com/developers/docs/reference#snowflakes
 */
const val DISCORD_EPOCH = 1420070400000L
fun Instant.asDiscordSnowflake(): Long {
	return (toEpochMilli() - DISCORD_EPOCH) shl 22
}

internal fun UriComponentsBuilder.namedQueryParam(name: String): UriComponentsBuilder {
	return queryParam(name, "{$name}")
}

internal fun <T> Timer.time(block: () -> T): T {
	return Timer.start().let {
		try {
			block.invoke()
		} finally {
			it.stop(this)
		}
	}
}

internal suspend fun <T> Timer.timeSuspending(block: suspend () -> T): T {
	return Timer.start().let {
		try {
			block.invoke()
		} finally {
			it.stop(this)
		}
	}
}


// Copies of Kotlin Time niceties for Java Time

internal val Number.millis: Duration get() = Duration.ofMillis(toLong())
internal val Number.seconds: Duration get() = Duration.ofSeconds(toLong())
internal val Number.minutes: Duration get() = Duration.ofMinutes(toLong())

internal suspend fun delay(duration: Duration): Unit = kotlinx.coroutines.delay(duration.toKotlinDuration())
