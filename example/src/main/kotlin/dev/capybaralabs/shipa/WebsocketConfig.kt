package dev.capybaralabs.shipa

import io.undertow.connector.ByteBufferPool
import io.undertow.server.DefaultByteBufferPool
import io.undertow.websockets.jsr.WebSocketDeploymentInfo
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * This just exists to get rid off a nasty Undertow warning
 *
 *
 * io.undertow.websockets.jsr: "UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used"
 */
@Configuration
class WebsocketConfig {

	@Bean
	fun undertowWebSocketServletWebServerCustomizer(): WebServerFactoryCustomizer<UndertowServletWebServerFactory> {
		return UndertowWebSocketServletWebServerCustomizer()
	}

	private class UndertowWebSocketServletWebServerCustomizer : WebServerFactoryCustomizer<UndertowServletWebServerFactory>, Ordered, DisposableBean {

		// Optimal size for direct buffers is 16kB according to Undertow docs
		// https://undertow.io/undertow-docs/undertow-docs-2.0.0/index.html#the-undertow-buffer-pool
		private val buffers: ByteBufferPool = DefaultByteBufferPool(true, 16 * 1024, 100, 12)

		override fun customize(factory: UndertowServletWebServerFactory) {
			factory.addDeploymentInfoCustomizers(
				UndertowDeploymentInfoCustomizer { deploymentInfo ->
					var info = deploymentInfo.servletContextAttributes[WebSocketDeploymentInfo.ATTRIBUTE_NAME] as WebSocketDeploymentInfo?
					if (info == null) {
						info = WebSocketDeploymentInfo()
						deploymentInfo.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, info)
					}
					info.buffers = buffers
				},
			)
		}

		override fun getOrder(): Int {
			return Ordered.LOWEST_PRECEDENCE
		}

		override fun destroy() {
			buffers.close()
		}
	}
}
