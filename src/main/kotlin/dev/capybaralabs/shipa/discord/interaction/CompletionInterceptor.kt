package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.concurrent.CompletableFuture
import org.springframework.web.servlet.AsyncHandlerInterceptor

class CompletionInterceptor : AsyncHandlerInterceptor {

	companion object {
		const val ATTRIBUTE = "CompletionInterceptor.afterComplete"
	}

	override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
		request.getAttribute(ATTRIBUTE)?.let { it as CompletableFuture<*> }?.complete(null)

		logger().trace("afterCompletion")
	}

	override fun afterConcurrentHandlingStarted(request: HttpServletRequest, response: HttpServletResponse, handler: Any) {
		logger().trace("afterConcurrentHandlingStarted")
	}
}
