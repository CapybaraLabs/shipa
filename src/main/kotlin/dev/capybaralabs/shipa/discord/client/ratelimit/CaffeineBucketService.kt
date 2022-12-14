package dev.capybaralabs.shipa.discord.client.ratelimit

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import org.springframework.stereotype.Service

private val ONE_MINUTE = Duration.ofMinutes(1)

interface BucketService {
	fun bucket(bucketKey: String): Bucket
	fun update(bucketKey: String, bucket: Bucket)
}

@Service
internal class CaffeineBucketService : BucketService {

	private val buckets = Caffeine.newBuilder()
		.expireAfter(object : Expiry<String, Bucket> {
			override fun expireAfterCreate(key: String, bucket: Bucket, currentTime: Long): Long =
				ONE_MINUTE.toNanos()

			override fun expireAfterUpdate(key: String, bucket: Bucket, currentTime: Long, currentDuration: Long): Long =
				calcExpire(bucket).toNanos()

			override fun expireAfterRead(key: String, bucket: Bucket, currentTime: Long, currentDuration: Long): Long =
				currentDuration
		})
		.build<String, Bucket>()

	override fun bucket(bucketKey: String): Bucket {
		return buckets.asMap().computeIfAbsent(bucketKey) { Bucket(it) }
	}

	override fun update(bucketKey: String, bucket: Bucket) {
		buckets.put(bucketKey, bucket)
	}


	// we want to keep entries until their next reset, or soonish after access/update
	private fun calcExpire(bucket: Bucket): Duration {
		val now = Instant.now()

		val soon = now.plus(ONE_MINUTE)
		val afterReset = bucket.nextReset.plus(ONE_MINUTE)

		val until = if (afterReset.isAfter(soon)) afterReset else soon

		return Duration.between(now, until)
	}

}

class Bucket(val name: String) {

	val mutex = Mutex()

	var discordName: String? = null

	var limit: Int = 5
	var tokens: Int = limit
	var nextReset: Instant = Instant.EPOCH
}
