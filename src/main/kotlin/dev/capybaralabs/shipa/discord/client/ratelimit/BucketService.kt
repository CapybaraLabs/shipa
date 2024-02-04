package dev.capybaralabs.shipa.discord.client.ratelimit

import dev.capybaralabs.shipa.discord.client.DiscordAuthToken

interface BucketService {
	fun bucket(token: DiscordAuthToken, bucketKey: BucketKey): Bucket
	fun update(token: DiscordAuthToken, bucketKey: BucketKey, bucket: Bucket)
}
