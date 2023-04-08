package dev.capybaralabs.shipa.discord.gateway

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.time.delay
import org.springframework.stereotype.Service

/**
 * We need to respect two ratelimits:
 * - Short term ratelimiting (max concurrency per 5 seconds)
 * - Long term ratelimiting (identifies per 24 hours)
 */
interface IdentifyLimiter {

	suspend fun awaitNext(botId: Long, concurrency: Int = 1)

}

@Service
internal class InMemoryIdentifyLimiter : IdentifyLimiter {

	// we need:
	// - max concurrency
	// - total allowed per 24h

	// by bot id
	val shortTermBuckets = ConcurrentHashMap<Long, Bucket>()


	override suspend fun awaitNext(botId: Long, concurrency: Int) {
		awaitShortTerm(botId, concurrency)

		// TODO await long term ratelimits
	}

	private suspend fun awaitShortTerm(botId: Long, concurrency: Int) {
		val bucket = shortTermBuckets.computeIfAbsent(botId) { Bucket(concurrency) }

		bucket.mutex.withLock {
			if (bucket.available > 0) {
				bucket.available = bucket.available - 1
			} else {
				val now = Instant.now()
				val nextReset = bucket.lastReset.plus(delay.toJavaDuration())

				if (nextReset.isAfter(now)) {
					val wait = Duration.between(now, nextReset)
					delay(wait)
					bucket.lastReset = nextReset
				} else {
					bucket.lastReset = now
				}

				bucket.available = bucket.concurrency - 1
			}
		}
	}

}


val delay = 6.seconds // 5 secs + 1 sec for network tolerance

internal class Bucket(
	val concurrency: Int = 1,
) {
	val mutex: Mutex = Mutex()

	var lastReset: Instant = Instant.now()
	var available: Int = concurrency
}
