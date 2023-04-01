package dev.capybaralabs.shipa.discord.gateway

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.model.IntBitflag
import java.util.Optional

/**
 * [Gateway Send Events](https://discord.com/developers/docs/topics/gateway-events#send-events)
 */
sealed interface GatewaySendEvent {
	@JsonIgnore
	fun op(): GatewayOpCode
}

/**
 * [Gateway Identify Event](https://discord.com/developers/docs/topics/gateway-events#identify-identify-structure)
 */
data class Identify(
	val token: String,
	val properties: ConnectionProperties,
	val compess: Boolean?,
	val largeThreshold: Int?,
	val shard: List<Int>?, // TODO can we use a type that restricts to two ints?
	val presence: UpdatePresence?,
	val intents: IntBitfield<GatewayIntent>,
) : GatewaySendEvent {

	override fun op() = GatewayOpCode.Identify
}

/**
 * [Identify Connection Properties](https://discord.com/developers/docs/topics/gateway-events#identify-identify-connection-properties)
 */
data class ConnectionProperties(
	val os: String,
	val browser: String, // library name
	val device: String, // library name
)

/**
 * [Gateway Presence Update](https://discord.com/developers/docs/topics/gateway-events#update-presence)
 */
data class UpdatePresence(
	val since: Optional<Int>,
	val activities: List<SendPresenceActivity>,
	val status: PresenceStatus,
	val afk: Boolean,
)

/**
 * [Presence Activity](https://discord.com/developers/docs/topics/gateway-events#activity-object)
 */
data class SendPresenceActivity(
	val name: String,
	val type: Int,
	val url: Optional<String>?,
)

/**
 * [Presence Status](https://discord.com/developers/docs/topics/gateway-events#update-presence-status-types)
 */
enum class PresenceStatus {
	ONLINE,
	DND,
	IDLE,
	INVISIBLE,
	OFFLINE,
}


/**
 * [Discord Gateway Intents](https://discord.com/developers/docs/topics/gateway#list-of-intents)
 */
enum class GatewayIntent(override val value: Int) : IntBitflag {
	GUILDS(1 shl 0),
	GUILD_MEMBERS(1 shl 1),
	GUILD_MODERATION(1 shl 2),
	GUILD_EMOJIS_AND_STICKERS(1 shl 3),
	GUILD_INTEGRATIONS(1 shl 4),
	GUILD_WEBHOOKS(1 shl 5),
	GUILD_INVITES(1 shl 6),
	GUILD_VOICE_STATES(1 shl 7),
	GUILD_PRESENCES(1 shl 8),
	GUILD_MESSAGES(1 shl 9),
	GUILD_MESSAGE_REACTIONS(1 shl 10),
	GUILD_MESSAGE_TYPING(1 shl 11),
	DIRECT_MESSAGES(1 shl 12),
	DIRECT_MESSAGE_REACTIONS(1 shl 13),
	DIRECT_MESSAGE_TYPING(1 shl 14),
	MESSAGE_CONTENT(1 shl 15),
	GUILD_SCHEDULED_EVENTS(1 shl 16),
	AUTO_MODERATION_CONFIGURATION(1 shl 20),
	AUTO_MODERATION_EXECUTION(1 shl 21),
}


/**
 * [Gateway Resume Event](https://discord.com/developers/docs/topics/gateway-events#resume)
 */
data class Resume(
	val token: String,
	val sesssionId: String,
	val seq: Int,
) : GatewaySendEvent {

	override fun op() = GatewayOpCode.Resume
}

/**
 * [Gateway Heartbeat Event](https://discord.com/developers/docs/topics/gateway-events#heartbeat)
 */
data class Heartbeat(
	@JsonValue val seq: Int?,
) : GatewaySendEvent, GatewayReceiveEvent {

	override fun op() = GatewayOpCode.Heartbeat
}


/**
 * [Gateway Request Guild Members Event](https://discord.com/developers/docs/topics/gateway-events#request-guild-members)
 */
data class RequestGuildMembers(
	val guildId: Long,
	val query: String?,
	val limit: Int,
	val presences: Boolean?,
	val userIds: List<Long>?,
	val nonce: String?,
) : GatewaySendEvent {

	override fun op() = GatewayOpCode.RequestGuildMembers
}


//TODO UpdateVoiceState
//TODO UpdatePresence
