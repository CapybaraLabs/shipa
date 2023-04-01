package dev.capybaralabs.shipa.discord.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import dev.capybaralabs.shipa.discord.gateway.OutboundGatewayCloseCode.ZombieConnection
import dev.capybaralabs.shipa.discord.model.IntBitfield
import dev.capybaralabs.shipa.discord.toStringByReflection
import dev.capybaralabs.shipa.logger
import java.net.URI
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler


/**
 * TODO
 * 	- websocket connection
 * 		- state management, actors, etc
 * 	- intents
 * 		ez?
 * 	- big bot sharding / identify ratelimiting
 * 	    - state management, actors?
 * 	- multi-bot-support
 *
 * encoding & compression https://discord.com/developers/docs/topics/gateway#encoding-and-compression
 *
 * 	out of scope:
 * 	 custom shard ranges.
 */

class Gateway(
	private val config: StaticGatewayConfig,
	private val mapper: ObjectMapper,
	private val scope: CoroutineScope,
	private val identifyLimiter: IdentifyLimiter,
	private val dispatchHandler: DispatchHandler,
) {

	private val client: WebSocketClient = StandardWebSocketClient()
	private val actor: SendChannel<InternalGatewayEvent> = scope.gatewayActor()
	private val handler = WebsocketHandlerAdapter(mapper, actor, config)

	private var state: GatewayConnectionState
	private lateinit var session: WebSocketSessionSender

	init {
		state = ConnectAwaitingHello()
		scope.async { session = createConnection() }
			.invokeOnCompletion { throwable ->
				if (throwable != null) {
					logger().error("Error during initial connect", throwable)
				}
			}
	}

	private suspend fun createConnection(): WebSocketSessionSender {
		// TODO consider refreshing the URI from the static config
		return connect(config.gatewayUri)
	}

	private suspend fun resumeConnection(resumeUri: URI): WebSocketSessionSender {
		return connect(resumeUri)
	}

	// TODO: Reset / Throw away the actor message queue when creating a new connection
	private suspend fun connect(uri: URI): WebSocketSessionSender {
		val headers = WebSocketHttpHeaders()
		logger().debug("{} Connecting to gateway at {}", config.renderShardInfo(), uri)
		val session = client.execute(handler, headers, uri).await()
		return WebSocketSessionSender(session, mapper, config)
	}


	@Suppress("RemoveExplicitTypeArguments") // https://youtrack.jetbrains.com/issue/KT-52757/Type-inference-for-builders-fails-if-inferred-from-a-function
	@OptIn(ObsoleteCoroutinesApi::class)
	private fun CoroutineScope.gatewayActor() = actor<InternalGatewayEvent>(
		capacity = 100, // TODO configurable?
	) {
		// single threaded processing in here
		for (event in channel) {
			state.handleEvent(event)
		}
	}

	sealed interface GatewayConnectionState {
		// override to implement behaviour
		suspend fun handleEvent(event: InternalGatewayEvent) {
			logger().error("{} Unhandled event {} during state {}", config().renderShardInfo(), event, this)
		}

		fun config(): StaticGatewayConfig
	}

	/**
	 * Behaviours for the gateway connection states.
	 */
	abstract inner class GatewayConnectionBehaviour : GatewayConnectionState {

		override fun toString(): String {
			return this.toStringByReflection()
		}

		override fun config(): StaticGatewayConfig {
			return config
		}

		override suspend fun handleEvent(event: InternalGatewayEvent) {

			when (event) {
				// Connection Events

				ConnectionEstablished -> {
					logger().info("{} Connected gateway", config.renderShardInfo())
				}

				is ConnectionClosed -> {
					val closeCode = event.closeCode
					val reason = event.reason ?: "[no reason provided]"
					if (closeCode != null && !closeCode.reconnect) {
						logger().warn("{} Disconnected with {} {} and reason {} during {}, and not reconnecting", config.renderShardInfo(), closeCode, closeCode.value, reason, state)
						state = Stopped()
						return
					}

					if (closeCode == null) {
						// Resume when unknown 3. https://discord.com/developers/docs/topics/gateway#resuming
						logger().warn("{} Unhandled close code {} with reason {} during {}", config.renderShardInfo(), event.code, reason, state)
					} else {
						logger().info("{} Disconnected with {} {} and reason {} during {}, reconnecting", config.renderShardInfo(), closeCode, closeCode.value, reason, state)
					}

					resumeOrReconnect()
					return
				}

				// Discord Events
				is InvalidSession -> {
					if (!event.resumable) {
						logger().error("{} Invalid session and not resumable during {}", config.renderShardInfo(), state)
					} else {
						// TODO do we ALSO receive a ConnectionClosed right afterwards? how to avoid double reconnect?
						resumeOrReconnect()
						return
					}
				}

				else -> super.handleEvent(event)
			}
		}

		protected suspend fun resumeOrReconnect() {
			val identified: Identified? = this.let { if (it is Identified) it else null }
			val sequence = identified?.heartbeatState?.sequenceRef?.get()
			val alreadyResuming = state is Resuming // avoid getting caught in a resume loop. rather do a full reconnect

			if (!alreadyResuming && identified != null && sequence != null) {
				// Resume
				state = ResumeAwaitingHello(identified.heartbeatState, identified.resumeGatewayUrl, identified.sessionId, sequence)
				session = resumeConnection(identified.resumeGatewayUrl)
			} else {
				// Reconnect
				state = Reconnecting()
				session = createConnection()
			}
		}
	}

	interface Identified {
		val heartbeatState: HeartbeatState
		val resumeGatewayUrl: URI
		val sessionId: String
	}

	abstract inner class HeartbeatLaunching : GatewayConnectionBehaviour() {
		private val logger = logger() // Avoid namespacing the logger to CoroutineScope

		// TODO catch exceptions to avoid bubbling up and cancelling parent? what is actually the parent here?
		protected fun CoroutineScope.launchHeartbeatLoop(interval: Duration, sequenceRef: AtomicReference<Int?>, ackRef: AtomicBoolean): Job = launch {
			logger.debug("{} Launching heartbeat loop with interval {}", config.renderShardInfo(), interval)

			val jitteredInitialDelay = interval.times(ThreadLocalRandom.current().nextDouble(1.0))
			logger.debug("{} Initial jittered delay is {}", config.renderShardInfo(), jitteredInitialDelay)
			delay(jitteredInitialDelay)

			var lastSeqSent: Int? = null
			while (isActive) {
				val lastAcked = ackRef.getAndSet(false)
				if (!lastAcked) {
					logger.warn("{} Last heartbeart seq {} not acked. Zombie connection? Reconnecting...", config.renderShardInfo(), lastSeqSent)
					session.close(ZombieConnection, "Zombie connection")
					return@launch
				}
				val seq = sequenceRef.get()
				session.send(Heartbeat(seq))
				lastSeqSent = seq
				delay(interval)
			}
		}
	}

	open inner class ConnectAwaitingHello : HeartbeatLaunching() {
		override suspend fun handleEvent(event: InternalGatewayEvent) {
			when (event) {
				is Hello -> {
					val sequenceRef = AtomicReference<Int?>(null)
					val heartbeatAckRef = AtomicBoolean(true)
					val heartbeatLoop = scope.launchHeartbeatLoop(event.heartbeatDuration, sequenceRef, heartbeatAckRef)
					val heartbeatState = HeartbeatState(event.heartbeatDuration, heartbeatLoop, sequenceRef, heartbeatAckRef)

					state = AwaitingReady(heartbeatState)

					//TODO do we need to do anything with the Job object?
					// what happens when further state changes happen before the job completes?
					scope.launch {
						// This can take a while, we should not block the state actor from processing other events.
						identifyLimiter.awaitNext(0L) // TODO multi bot support
						val identify = Identify(
							config.token,
							config.connectionProperties,
							false,
							250,
							listOf(config.shardId, config.totalShards),
							null,
							config.intents,
						)
						session.send(identify)
					}
					return
				}

				else -> super.handleEvent(event)
			}
		}
	}


	data class HeartbeatState(
		val interval: Duration,
		val loop: Job,
		val sequenceRef: AtomicReference<Int?>,
		val ackRef: AtomicBoolean,
	)


	abstract inner class HeartbeatingBehaviour(
		val heartbeatState: HeartbeatState,
	) : GatewayConnectionBehaviour() {

		override suspend fun handleEvent(event: InternalGatewayEvent) {
			if (event is ConnectionClosed) {
				heartbeatState.loop.cancel()
			}

			if (event is Dispatch<*>) {
				updateSequence(event.sequence)
			}

			if (event is Heartbeat) {
				logger().debug("{} Discord requesting immediate heartbeat", config.renderShardInfo())
				session.send(Heartbeat(heartbeatState.sequenceRef.get()))
				return
			}

			if (event is HeartbeatAck) {
				logger().debug("{} Heartbeat acked by Discord", config.renderShardInfo())
				val previousAck = heartbeatState.ackRef.getAndSet(true)
				if (previousAck) {
					logger().warn("{} Received duplicate heartbeat ack", config.renderShardInfo())
				}
				return
			}

			super.handleEvent(event)
		}

		fun updateSequence(nextSequence: Int) {
			val previousSequence = this.heartbeatState.sequenceRef.getAndSet(nextSequence)
			checkLinearity(previousSequence, nextSequence)
		}

		private fun checkLinearity(previousSequence: Int?, nextSequence: Int) {
			if (previousSequence == null && nextSequence != 1) {
				logger().warn("{} Initial sequence mismatch, expected 1 but got {}", config.renderShardInfo(), nextSequence)
			}
			if (previousSequence != null && previousSequence + 1 != nextSequence) {
				logger().warn("{} Sequence mismatch, expected {} but got {}", config.renderShardInfo(), previousSequence + 1, nextSequence)
			}
		}
	}

	inner class AwaitingReady(
		heartbeatState: HeartbeatState,
	) : HeartbeatingBehaviour(heartbeatState) {

		override suspend fun handleEvent(event: InternalGatewayEvent) {
			when (event) {
				is Dispatch.Ready -> {
					updateSequence(event.sequence)
					state = Connected(heartbeatState, URI.create(event.data.resumeGatewayUrl), event.data.sessionId)
				}

				else -> super.handleEvent(event)
			}
		}
	}

	inner class Connected(
		heartbeatState: HeartbeatState,
		override val resumeGatewayUrl: URI,
		override val sessionId: String,
	) : HeartbeatingBehaviour(heartbeatState), Identified {

		override suspend fun handleEvent(event: InternalGatewayEvent) {
			when (event) {
				is Dispatch<*> -> {
					logger().info("{} Receiving dispatch {} {}", config.renderShardInfo(), event.sequence, event.name)
					updateSequence(event.sequence)
					dispatchHandler.handle(event)
					return
				}

				else -> super.handleEvent(event)
			}
		}
	}

	// waiting for connection + Hello
	inner class ResumeAwaitingHello(
		private val heartbeatState: HeartbeatState,
		private val resumeGatewayUrl: URI,
		private val sessionId: String,
		private val lastReceivedSequence: Int,
	) : HeartbeatLaunching() {
		override suspend fun handleEvent(event: InternalGatewayEvent) {
			when (event) {
				ConnectionEstablished -> {
					logger().info("{} Connected gateway during resuming", config.renderShardInfo())

					// is Hello guaranteed on resume?
				}

				is Hello -> {
					logger().info("{} Received Hello during resuming", config.renderShardInfo())

					val heartbeatLoop = scope.launchHeartbeatLoop(event.heartbeatDuration, heartbeatState.sequenceRef, heartbeatState.ackRef)
					val freshHeartbeat = heartbeatState.copy(loop = heartbeatLoop)

					state = Resuming(freshHeartbeat, resumeGatewayUrl, sessionId)
					session.send(Resume(config.token, sessionId, lastReceivedSequence))
				}

				else -> super.handleEvent(event)
			}
		}
	}

	// waiting for missing events + Resumed, or InvalidSession
	inner class Resuming(
		heartbeatState: HeartbeatState,
		override val resumeGatewayUrl: URI,
		override val sessionId: String,
	) : HeartbeatingBehaviour(heartbeatState), Identified {


		override suspend fun handleEvent(event: InternalGatewayEvent) {
			when (event) {
				is Dispatch<*> -> {
					updateSequence(event.sequence)
					dispatchHandler.handle(event)
					return
				}

				is Resumed -> {
					logger().info("{} Resumed session", config.renderShardInfo())
					state = Connected(heartbeatState, resumeGatewayUrl, sessionId)
					return
				}

				else -> super.handleEvent(event)
			}
		}
	}

	inner class Reconnecting : ConnectAwaitingHello()
	inner class Stopped : GatewayConnectionBehaviour()


}


sealed interface InternalGatewayEvent

object ConnectionEstablished : InternalGatewayEvent
data class ConnectionClosed(val closeCode: GatewayCloseCode?, val code: Int, val reason: String?) : InternalGatewayEvent


class WebsocketHandlerAdapter(
	private val mapper: ObjectMapper,
	private val actor: SendChannel<InternalGatewayEvent>,
	private val config: StaticGatewayConfig,
) : TextWebSocketHandler() {

	// NOTE We want to avoid blocking the (XNIO) I/O threads here
	private fun tryTell(event: InternalGatewayEvent) {
		val sendResult = actor.trySend(event)
		if (sendResult.isSuccess) {
			return
		}
		if (sendResult.isFailure) {
			if (sendResult.isClosed) {
				logger().warn("{} Gateway connection actor closed, failed to publish {}", config.renderShardInfo(), event)
			} else {
				throw IllegalStateException("${config.renderShardInfo()} Failed to publish $event to actor", sendResult.exceptionOrNull())
			}
		}
	}


	override fun afterConnectionEstablished(session: WebSocketSession) {
		tryTell(ConnectionEstablished)
	}

	override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
		logger().warn("{} Transport error in gateway connection", config.renderShardInfo(), exception)
		super.handleTransportError(session, exception)

		// need not throw an event, afterConnectionClosed will be called
	}

	override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
		val closeCode = InboundGatewayCloseCode.values().find { it.value == status.code }
			?: OutboundGatewayCloseCode.values().find { it.value == status.code }
		tryTell(ConnectionClosed(closeCode, status.code, status.reason))
	}


	override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
		val receiveEvent = parseReceiveEvent(message.payload)

		tryTell(receiveEvent)
	}

	private fun parseReceiveEvent(message: String): GatewayReceiveEvent {
		logger().debug("{} Receiving event {}", config.renderShardInfo(), message)
		val jsonTree = mapper.readTree(message)

		return when (val opcode = jsonTree.get("op").asInt()) {
			0 -> {
				val sequence = jsonTree.get("s").asInt()
				when (val name = jsonTree.get("t").asText()) {
					"READY" -> Dispatch.Ready(sequence, name, jsonTree.get("d").let { mapper.treeToValue(it, Dispatch.Ready.ReadyData::class.java) })
					else -> Dispatch.Unknown(sequence, name)
				}
			}

			7 -> Reconnect
			9 -> InvalidSession(jsonTree.get("d").asBoolean())
			10 -> mapper.treeToValue(jsonTree.get("d"), Hello::class.java)
			11 -> HeartbeatAck
			1 -> Heartbeat(jsonTree.get("d").asInt())
			else -> throw IllegalArgumentException("Unknown receive opcode: $opcode")
		}

	}

	override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
		session.sendMessage(PongMessage())
	}
}

class WebSocketSessionSender(
	private val session: WebSocketSession,
	private val mapper: ObjectMapper,
	private val config: StaticGatewayConfig,
) {
	fun send(event: GatewaySendEvent) {
		val payload = mapOf(
			"op" to event.op(),
			"d" to event,
		)
		val payloadJson = mapper.writeValueAsString(payload)
		logger().debug("{} Sending {}", config.renderShardInfo(), payloadJson)
		session.sendMessage(TextMessage(payloadJson))
	}

	fun close(closeCode: OutboundGatewayCloseCode, reason: String) {
		session.close(CloseStatus(closeCode.value, reason))
	}
}

data class StaticGatewayConfig(
	val gatewayUri: URI,
	val token: String,
	val shardId: Int,
	val totalShards: Int,
	val intents: IntBitfield<GatewayIntent>,
	val connectionProperties: ConnectionProperties,
) {
	fun renderShardInfo(): String {
		return "[${shardId}/${totalShards}]"
	}
}
