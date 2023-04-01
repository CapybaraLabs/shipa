package dev.capybaralabs.shipa.discord.gateway

import org.springframework.web.socket.CloseStatus

/**
 * [Discord Gateway Close Codes](https://discord.com/developers/docs/topics/opcodes-and-status-codes#gateway-gateway-close-event-codes)
 */
interface GatewayCloseCode {
	val value: Int
	val reconnect: Boolean
}

enum class InboundGatewayCloseCode(
	override val value: Int,
	override val reconnect: Boolean,
) : GatewayCloseCode {

	// Discord Official Codes
	UnknownError(4000, true), // We're not sure what went wrong. Try reconnecting?
	UnknownOpcode(4001, true), // You sent an invalid Gateway opcode or an invalid payload for an opcode. Don't do that!
	DecodeError(4002, true), // You sent an invalid payload to Discord. Don't do that!
	NotAuthenticated(4003, true), // You sent us a payload prior to identifying.
	AuthenticationFailed(4004, false), // The account token sent with your identify payload is incorrect.
	AlreadyAuthenticated(4005, true), // You sent more than one identify payload. Don't do that!
	InvalidSeq(4007, true), // The sequence sent when resuming the session was invalid. Reconnect and start a new session.
	Ratelimited(4008, true), // Woah nelly! You're sending payloads to us too quickly. Slow it down! You will be disconnected on receiving this.
	SessionTimedOut(4009, true), // Your session timed out. Reconnect and start a new one.
	InvalidShard(4010, false), // You sent us an invalid shard when identifying.
	ShardingRequired(4011, false), // The session would have handled too many guilds - you are required to shard your connection in order to connect.
	InvalidApiVersion(4012, false), // You sent an invalid version for the gateway.
	InvalidIntents(4013, false), // You sent an invalid intent for a Gateway Intent. You may have incorrectly calculated the bitwise value.
	DisallowedIntents(4014, false), // You sent a disallowed intent for a Gateway Intent. You may have tried to specify an intent that you have not enabled or are not approved for.

	// Expected Codes from regular Websocket operation
	ClosedAbnormally(1006, true),
	ServerError(1011, true), // we receive this for some reason when testing resumes. it loops, so should force reconnects
}

// Our Client side close codes
enum class OutboundGatewayCloseCode(
	override val value: Int,
	override val reconnect: Boolean,
) : GatewayCloseCode {

	ZombieConnection(CloseStatus.POLICY_VIOLATION.code, true),

//	InternalResume(9001, true),
//	InternalReconnect(9002, true),
//	InternalAbandon(9003, false),
}
