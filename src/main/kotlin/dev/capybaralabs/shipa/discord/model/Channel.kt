package dev.capybaralabs.shipa.discord.model

import com.fasterxml.jackson.annotation.JsonValue

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
