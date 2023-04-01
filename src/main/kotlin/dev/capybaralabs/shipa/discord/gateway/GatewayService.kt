package dev.capybaralabs.shipa.discord.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.discord.client.entity.DiscordEntityRestService
import dev.capybaralabs.shipa.discord.model.IntBitfield
import java.net.URI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

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
	private val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(100)) // TODO configurable / finetuning defaults


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
				Gateway(
					config, mapper, scope, identifyLimiter,
					object : DispatchHandler {
						override fun handle(event: Dispatch<*>) {
							//
						}

						override fun invalidate(shardId: Int) {
							//
						}
					},
				)
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
		val shards: MutableMap<Int, Gateway>,
	)
}
