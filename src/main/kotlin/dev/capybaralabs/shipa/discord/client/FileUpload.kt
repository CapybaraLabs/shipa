package dev.capybaralabs.shipa.discord.client

import dev.capybaralabs.shipa.discord.model.PartialAttachment
import java.io.File

data class FileUpload(
	val name: String,
	val data: File,
	val description: String? = null,
) {

	fun asAttachment(index: Long): PartialAttachment {
		return PartialAttachment(index, name, description)
	}
}
