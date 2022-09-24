package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.logger
import java.util.concurrent.CompletableFuture
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
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
