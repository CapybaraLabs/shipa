package dev.capybaralabs.shipa.discord.gateway

import com.fasterxml.jackson.annotation.JsonValue

/**
 * [Discord Gateway Op Codes](https://discord.com/developers/docs/topics/opcodes-and-status-codes#gateway-gateway-opcodes)
 */
enum class GatewayOpCode(@JsonValue val value: Int) {

	// Send
	Identify(2), // Starts a new session during the initial handshake.
	PresenceUpdate(3), // Update the client's presence.
	VoiceStateUpdate(4), // Used to join/leave or move between voice channels.
	Resume(6), // Resume a previous session that was disconnected.
	RequestGuildMembers(8), // Request information about offline guild members in a large guild.

	// Receive
	Dispatch(0), // An event was dispatched.
	Reconnect(7), // You should attempt to reconnect and resume immediately.
	InvalidSession(9), // The session has been invalidated. You should reconnect and identify/resume accordingly.
	Hello(10), // Sent immediately after connecting, contains the heartbeat_interval to use.
	HeartbeatAck(11), // Sent in response to receiving a heartbeat to acknowledge that it has been received.

	// Both
	Heartbeat(1), // Fired periodically by the client to keep the connection alive.

}
