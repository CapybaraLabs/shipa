package dev.capybaralabs.shipa.discord.client.ratelimit

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import org.springframework.stereotype.Service


@Service
internal class CaffeineBucketService : BucketService {

	private val oneMinute = Duration.ofMinutes(1)

	private val buckets = Caffeine.newBuilder()
		.expireAfter(
			object : Expiry<BucketKey, Bucket> {
				override fun expireAfterCreate(key: BucketKey, bucket: Bucket, currentTime: Long): Long =
					oneMinute.toNanos()

				override fun expireAfterUpdate(key: BucketKey, bucket: Bucket, currentTime: Long, currentDuration: Long): Long =
					calcExpire(bucket).toNanos()

				override fun expireAfterRead(key: BucketKey, bucket: Bucket, currentTime: Long, currentDuration: Long): Long =
					currentDuration
			},
		)
		.build<BucketKey, Bucket>()

	override fun bucket(bucketKey: BucketKey): Bucket {
		return buckets.asMap().computeIfAbsent(bucketKey) { Bucket(it) }
	}

	override fun update(bucketKey: BucketKey, bucket: Bucket) {
		buckets.put(bucketKey, bucket)
	}


	// we want to keep entries until their next reset, or soonish after access/update
	private fun calcExpire(bucket: Bucket): Duration {
		val now = Instant.now()

		val soon = now.plus(oneMinute)
		val afterReset = bucket.nextReset.plus(oneMinute)

		val until = if (afterReset.isAfter(soon)) afterReset else soon

		return Duration.between(now, until)
	}

}

class Bucket(val key: BucketKey) {

	val mutex = Mutex()

	var discordName: String? = null

	var limit: Int = 5
	var tokens: Int = limit
	var nextReset: Instant = Instant.EPOCH
}
