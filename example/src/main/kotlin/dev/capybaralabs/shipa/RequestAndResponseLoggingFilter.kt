package dev.capybaralabs.shipa

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Locale
import javax.servlet.FilterChain
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

/**
 * Adapted from https://gist.github.com/michael-pratt/89eb8800be8ad47e79fe9edab8945c69
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@WebFilter(urlPatterns = ["/*"])
@Component
class RequestAndResponseLoggingFilter(private val objectMapper: ObjectMapper) : OncePerRequestFilter() {

	/**
	 * List of HTTP headers whose values should not be logged.
	 */
	private val sensitiveHeaders = listOf(
		"authorization",
		"proxy-authorization"
	)

	private val visibleTypes = listOf(
		MediaType.valueOf("text/*"),
		MediaType.APPLICATION_FORM_URLENCODED,
		MediaType.APPLICATION_JSON,
		MediaType.APPLICATION_XML,
		MediaType.valueOf("application/*+json"),
		MediaType.valueOf("application/*+xml"),
		MediaType.MULTIPART_FORM_DATA
	)

	override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
		if (!logger.isInfoEnabled) {
			filterChain.doFilter(request, response)
			return
		}

		doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain)
	}

	override fun shouldNotFilterAsyncDispatch(): Boolean {
		return false
	}

	private fun doFilterWrapped(request: ContentCachingRequestWrapper, response: ContentCachingResponseWrapper, filterChain: FilterChain) {
		val msg = StringBuilder()
		try {
			beforeRequest(request, msg)
			filterChain.doFilter(request, response)
		} catch (e: Exception) {
			logger().error("LOL", e)
			throw e
		} finally {
			afterRequest(request, response, msg)
			logger().info(msg.toString())
			response.copyBodyToResponse()
		}
	}

	private fun beforeRequest(request: ContentCachingRequestWrapper, msg: StringBuilder) {
		msg.append("\n-- REQUEST --\n")
		logRequestHeader(request, request.remoteAddr + "|>", msg)
	}

	private fun afterRequest(request: ContentCachingRequestWrapper, response: ContentCachingResponseWrapper, msg: StringBuilder) {
		logRequestBody(request, request.remoteAddr + "|>", msg)
		msg.append("\n-- RESPONSE --\n")
		logResponse(response, request.remoteAddr + "|<", msg)
	}


	private fun logRequestHeader(request: ContentCachingRequestWrapper, prefix: String, msg: StringBuilder) {
		val queryString = request.queryString
		if (queryString != null) {
			msg.append("$prefix ${request.method} ${request.requestURI}?$queryString\n")
		} else {
			msg.append("$prefix ${request.method} ${request.requestURI}\n")
		}

		request.headerNames.asSequence().forEach { headerName ->
			request.getHeaders(headerName).asSequence().forEach { headerValue ->
				if (isSensitiveHeader(headerName)) {
					msg.append("$prefix $headerName: *******\n")
				} else {
					msg.append("$prefix $headerName: $headerValue\n")
				}
			}
		}
		msg.append("$prefix\n")
	}

	private fun logRequestBody(request: ContentCachingRequestWrapper, prefix: String, msg: StringBuilder) {
		val content = request.contentAsByteArray
		if (content.isNotEmpty()) {
			logContent(content, request.contentType, request.characterEncoding, prefix, msg)
		}
	}

	private fun logResponse(response: ContentCachingResponseWrapper, prefix: String, msg: StringBuilder) {
		val status = response.status
		msg.append("$prefix $status ${HttpStatus.valueOf(status).reasonPhrase}\n")

		response.headerNames.asSequence().forEach { headerName ->
			response.getHeaders(headerName).asSequence().forEach { headerValue ->
				if (isSensitiveHeader(headerName)) {
					msg.append("$prefix $headerName: *******\n")
				} else {
					msg.append("$prefix $headerName: $headerValue\n")
				}
			}
		}
		msg.append("$prefix\n")
		val content = response.contentAsByteArray
		if (content.isNotEmpty()) {
			logContent(content, response.contentType, response.characterEncoding, prefix, msg)
		}
	}

	private fun logContent(content: ByteArray, contentType: String, contentEncoding: String, prefix: String, msg: StringBuilder) {
		val mediaType = MediaType.valueOf(contentType)
		val visible = visibleTypes.stream().anyMatch { it.includes(mediaType) }
		if (visible) {
			try {
				var contentString = String(content, Charset.forName(contentEncoding))
				if (mediaType == MediaType.APPLICATION_JSON) {
					val value = objectMapper.readValue(contentString, Any::class.java)
					contentString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value)
				}
				contentString.split("\r\n|\r|\n").forEach { line ->
					msg.append("$prefix $line\n")
				}
			} catch (e: UnsupportedEncodingException) {
				msg.append("$prefix [${content.size} bytes content]\n")
			}
		} else {
			msg.append("$prefix [${content.size} bytes content]\n")
		}
	}

	/**
	 * Determine if a given header name should have its value logged.
	 * @param headerName HTTP header name.
	 * @return True if the header is sensitive (i.e. its value should **not** be logged).
	 */
	private fun isSensitiveHeader(headerName: String): Boolean {
		return sensitiveHeaders.contains(headerName.lowercase(Locale.getDefault()))
	}

	private fun wrapRequest(request: HttpServletRequest): ContentCachingRequestWrapper {
		return if (request is ContentCachingRequestWrapper) {
			request
		} else {
			ContentCachingRequestWrapper(request)
		}
	}

	private fun wrapResponse(response: HttpServletResponse): ContentCachingResponseWrapper {
		return if (response is ContentCachingResponseWrapper) {
			response
		} else {
			ContentCachingResponseWrapper(response)
		}
	}

}
