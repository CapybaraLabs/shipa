package dev.capybaralabs.shipa.discord.gateway

import dev.capybaralabs.shipa.discord.model.User
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed interface GatewayReceiveEvent : InternalGatewayEvent

/**
 * [Gateway Hello Event](https://discord.com/developers/docs/topics/gateway-events#hello)
 */
data class Hello(
	val heartbeatInterval: Int, // milliseconds
) : GatewayReceiveEvent {

	val heartbeatDuration: Duration
		get() {
			return heartbeatInterval.milliseconds
		}
}

object HeartbeatAck : GatewayReceiveEvent

/**
 * [Unavailable Guild](https://discord.com/developers/docs/resources/guild#unavailable-guild-object)
 */
data class UnavailableGuild(
	val id: Long,
	val unavailable: Boolean,
)

/**
 * [Gateway Resumed Event](https://discord.com/developers/docs/topics/gateway-events#resumed)
 */
object Resumed : GatewayReceiveEvent

/**
 * [Gateway Reconnect Event](https://discord.com/developers/docs/topics/gateway-events#reconnect)
 */
object Reconnect : GatewayReceiveEvent


/**
 * [Gateway Invalid Session Event](https://discord.com/developers/docs/topics/gateway-events#invalid-session)
 */
data class InvalidSession(
	val resumable: Boolean,
) : GatewayReceiveEvent

interface Dispatch<D> : GatewayReceiveEvent {
	val sequence: Int
	val name: String
	val data: D?

	data class Unknown(
		override val sequence: Int,
		override val name: String,
		override val data: Nothing? = null,
	) : Dispatch<Nothing>

	/**
	 * [Gateway Ready Event](https://discord.com/developers/docs/topics/gateway-events#ready)
	 */
	data class Ready(
		override val sequence: Int,
		override val name: String,
		override val data: ReadyData,
	) : Dispatch<Ready.ReadyData> {

		data class ReadyData(
			val v: Int, // api version
			val user: User,
			val guilds: List<UnavailableGuild>,
			val sessionId: String,
			val resumeGatewayUrl: String,
			val shard: List<Int>?, // TODO can we use a type that restricts to two ints?
			//	val application: PartialApplication, // just id and flags
		)
	}
}
