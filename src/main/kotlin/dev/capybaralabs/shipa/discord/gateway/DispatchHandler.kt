package dev.capybaralabs.shipa.discord.gateway

/**
 * Receive [Dispatch] events from the Discord Gateway.
 */
interface DispatchHandler {

	fun handle(event: Dispatch<*>)

	fun invalidate(shardId: Int) // botId ? totalShards?

}
