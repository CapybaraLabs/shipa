package dev.capybaralabs.shipa.discord.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.discord.client.entity.DiscordEntityRestService
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.logger
import java.net.URI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder


/**
 *
 * Coroutine Hierarchy
 *
 * The shards need to be in a certain coroutine hierarchy that supports individial shards getting restarted without
 * affecting the other shards or the rest of the application.
 * Inside a single shard, then the coroutines comprising it fail, it should cause the whole shard to be restarted.
 *
 * GatewayServie supervisorScope {
 * 	- GatewayScope shard1 regularScope {
 * 	  - actor
 * 	  - heartbeat loop
 * 	  - etc
 * 	  }
 * 	- shard2,
 * 	- shard3,
 * 	- etc
 *
 * }
 *
 *
 *
 */
@Service
class GatewayService(
	private val restService: DiscordEntityRestService,
	private val mapper: ObjectMapper,
	private val identifyLimiter: IdentifyLimiter,
) {

	// bot id -> shard id -> gateway
	// TODO threadsafety
	private val managedGateways: MutableMap<Long, BotGateway> = mutableMapOf()

	private val os = System.getProperty("os.name")
	private val connectionProperties = ConnectionProperties(os, "shipa", "shipa")

	@OptIn(ExperimentalCoroutinesApi::class)
	// TODO configurable / finetuning defaults
	private val supervisorScope = CoroutineScope(Dispatchers.IO.limitedParallelism(100) + SupervisorJob())


	fun launch(shards: Set<Int>, props: GatewayProps): BotGateway {
		// we can get the bot id from Discord by using the token

		val application = runBlocking { restService.application.fetchCurrentBotApplicationInfo() } //TODO blocking?  // TODO multitoken support
		val botId = application.id

		val botGateway = managedGateways.getOrPut(botId) {
			BotGateway(props.totalShards, mutableMapOf())
		}
		if (botGateway.totalShards != props.totalShards) {
			throw IllegalArgumentException("totalShards must be the same for all shards of a bot")
		}

		val uri = UriComponentsBuilder.fromUri(props.url)
			.queryParam("v", 10)
			.queryParam("encoding", "json")
			.toUriString()
			.let { URI.create(it) }

		for (shard in shards) {
			botGateway.shards.getOrPut(shard) {
				val config = StaticGatewayConfig(uri, props.token, shard, props.totalShards, props.intents, connectionProperties)
				val gatewayScope = GatewayScope(
					config, mapper, identifyLimiter,
					object : DispatchHandler {
						override fun handle(event: Dispatch<*>) {
							//
						}

						override fun invalidate(shardId: Int) {
							//
						}
					},
				)


				supervisorScope.async {
					gatewayScope.supervise()
				}.invokeOnCompletion { throwable ->
					if (throwable != null) {
						logger().error("GatewayScope failed", throwable)
					}
				}


				gatewayScope
			}
		}

		return botGateway
	}


	data class GatewayProps(
		val url: URI,
		val token: String,
		val intents: IntBitfield<GatewayIntent>,
		val totalShards: Int,
//		val presence: Presence,
	)

	class BotGateway(
		val totalShards: Int, // determined once when the first shard is created.
		//TODO threadsafety
		val shards: MutableMap<Int, GatewayScope>,
	)
}
