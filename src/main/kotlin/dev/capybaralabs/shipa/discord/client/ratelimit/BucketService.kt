package dev.capybaralabs.shipa.discord.client.ratelimit

interface BucketService {
	fun bucket(bucketKey: BucketKey): Bucket
	fun update(bucketKey: BucketKey, bucket: Bucket)
}
