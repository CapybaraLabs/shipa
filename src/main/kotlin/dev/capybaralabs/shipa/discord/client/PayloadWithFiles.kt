package dev.capybaralabs.shipa.discord.client

import dev.capybaralabs.shipa.discord.model.PartialAttachment
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.MultiValueMap

interface WithAttachments<SELF : WithAttachments<SELF>> {
	val attachments: List<PartialAttachment>?
	fun copyWithAttachments(attachments: List<PartialAttachment>): SELF
}

interface PayloadWithFiles<T : WithAttachments<T>> {
	val payload: T
	val files: List<FileUpload>?

	fun toPotentialMultipartBody(): PotentialMultipartBody<T> {

		val files = this.files
		val body: PotentialMultipartBody<T> = if (!files.isNullOrEmpty()) {

			val result = payload.copyWithAttachments(
				(payload.attachments ?: listOf()) + files.mapIndexed { index, fileUpload ->
					fileUpload.asAttachment(index.toLong())
				},
			)

			val multipartBodyBuilder = MultipartBodyBuilder()
			multipartBodyBuilder.part("payload_json", result)
			files.forEachIndexed { index, fileUpload ->

				val bytes = ByteArrayResource(
					fileUpload.data.inputStream().use { it.readAllBytes() },
				)
				multipartBodyBuilder.part("files[$index]", bytes).filename(fileUpload.name)
			}

			MultipartBody(multipartBodyBuilder.build())
		} else {
			NotMultipartBody(payload)
		}

		return body
	}
}

sealed interface PotentialMultipartBody<T> {
	val body: Any
}

data class MultipartBody<T>(override val body: MultiValueMap<String, HttpEntity<*>>) : PotentialMultipartBody<T>
data class NotMultipartBody<T : Any>(override val body: T) : PotentialMultipartBody<T>
