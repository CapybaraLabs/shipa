package dev.capybaralabs.shipa.discord

import java.time.Instant
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
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

// https://stackoverflow.com/a/59914470
fun Any.toStringByReflection(exclude: List<String> = listOf(), mask: List<String> = listOf()): String {
	val propsString = this::class.memberProperties
		.filter { exclude.isEmpty() || !exclude.contains(it.name) }
		.joinToString(", ") {
			if (!it.isAccessible) it.isAccessible = true
			val value = if (mask.isNotEmpty() && mask.contains(it.name)) "****" else it.getter.call(this).toString()
			"${it.name}=${value}"
		}

	return "${this::class.simpleName} [${propsString}]"
}
