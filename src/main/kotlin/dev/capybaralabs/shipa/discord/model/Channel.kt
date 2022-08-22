package dev.capybaralabs.shipa.discord.model

import com.fasterxml.jackson.annotation.JsonValue
import java.time.Instant
import java.util.Optional

/**
 * [Discord Channel Object](https://discord.com/developers/docs/resources/channel#channel-object)
 */
data class Channel(
	val id: Long,
	val type: ChannelType,
	val guildId: Long?,
	val position: Int?,
//	val permissionOverwrites: List<Overwrite>,
	val name: Optional<String>?,
	val topic: Optional<String>?,
	val nsfw: Boolean?,
	val lastMessageId: Optional<Long>?,
	val bitrate: Int?,
	val userLimit: Int?,
	val rateLimitPerUser: Int?,
	val recipients: List<User>?,
	val icon: Optional<String>?,
	val ownerId: Long?,
	val applicationId: Long?,
	val parentId: Optional<Long>?,
	val lastPinTimestamp: Optional<Instant>?,
	val rtcRegion: Optional<String>?,
	val videoQualityMode: Long?,
	val messageCount: Long?,
	val memberCount: Long?,
//	val threadMetadata : ThreadMetadata?,
//  val member: ThreadMember?,
	val defaultAutoArchiveDuration: Int?,
	val permissions: StringBitfield<Permission>?,
	val flags: IntBitfield<ChannelFlag>?,
	val totalMessageSent: Int?,
)

/**
 * [Discord Channel Type](https://discord.com/developers/docs/resources/channel#channel-object-channel-types)
 */
enum class ChannelType(@JsonValue val value: Int) {
	GUILD_TEXT(0),
	DM(1),
	GUILD_VOICE(2),
	GROUP_DM(3),
	GUILD_CATEGORY(4),
	GUILD_NEWS(5),
	GUILD_NEWS_THREAD(10),
	GUILD_PUBLIC_THREAD(11),
	GUILD_PRIVATE_THREAD(12),
	GUILD_STAGE_VOICE(13),
	GUILD_DIRECTORY(14),
	GUILD_FORUM(15),
}

enum class ChannelFlag(override val value: Int) : IntBitflag {
	PINNED(1 shl 1)
}
