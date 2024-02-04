package dev.capybaralabs.shipa.discord.client.ratelimit

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import dev.capybaralabs.shipa.discord.client.DiscordAuthToken
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import org.springframework.stereotype.Service


@Service
internal class CaffeineBucketService : BucketService {

	private data class TokenBucketKey(
		val token: DiscordAuthToken,
		val bucketKey: BucketKey,
	)

	private val oneMinute = Duration.ofMinutes(1)

	private val buckets = Caffeine.newBuilder()
		.expireAfter(
			object : Expiry<TokenBucketKey, Bucket> {
				override fun expireAfterCreate(key: TokenBucketKey, bucket: Bucket, currentTime: Long): Long =
					oneMinute.toNanos()

				override fun expireAfterUpdate(key: TokenBucketKey, bucket: Bucket, currentTime: Long, currentDuration: Long): Long =
					calcExpire(bucket).toNanos()

				override fun expireAfterRead(key: TokenBucketKey, bucket: Bucket, currentTime: Long, currentDuration: Long): Long =
					currentDuration
			},
		)
		.build<TokenBucketKey, Bucket>()

	override fun bucket(token: DiscordAuthToken, bucketKey: BucketKey): Bucket {
		return buckets.asMap().computeIfAbsent(TokenBucketKey(token, bucketKey)) { Bucket(it.bucketKey) }
	}

	override fun update(token: DiscordAuthToken, bucketKey: BucketKey, bucket: Bucket) {
		buckets.put(TokenBucketKey(token, bucketKey), bucket)
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
