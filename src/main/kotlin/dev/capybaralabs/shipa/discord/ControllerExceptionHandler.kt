package dev.capybaralabs.shipa.discord

import dev.capybaralabs.shipa.logger
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice
class ControllerExceptionHandler {

	private val message = "Something Went Wrong"

	@ExceptionHandler(Exception::class)
	fun exception(ex: Exception): ResponseEntity<Any> {
		logger().error(message, ex)
		return ResponseEntity(getBody(INTERNAL_SERVER_ERROR, ex), INTERNAL_SERVER_ERROR)
	}

	fun getBody(status: HttpStatus, ex: Exception): Map<String, Any> {
		val body = LinkedHashMap<String, Any>()
		body["message"] = message
		body["timestamp"] = Instant.now()
		body["status"] = status.value()
		body["error"] = status.reasonPhrase
		val sw = StringWriter()
		val pw = PrintWriter(sw)
		ex.printStackTrace(pw)
		val sStackTrace = sw.toString() // stack trace as a string
		body["exception"] = sStackTrace
		return body
	}


}
