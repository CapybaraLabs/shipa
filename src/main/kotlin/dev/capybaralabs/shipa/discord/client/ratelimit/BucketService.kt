package dev.capybaralabs.shipa.discord.client.ratelimit

interface BucketService {
	fun bucket(bucketKey: String): Bucket
	fun update(bucketKey: String, bucket: Bucket)
}
