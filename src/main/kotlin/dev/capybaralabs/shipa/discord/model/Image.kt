package dev.capybaralabs.shipa.discord.model

import dev.capybaralabs.shipa.discord.model.ImageFormatting.Format
import java.util.Base64

/**
 * [Discord Image Data](https://discord.com/developers/docs/reference#image-data)
 */
class Image(
	private val data: ByteArray,
	private val format: Format = Format.PNG, // only JPG, GIF, PNG
) {
	fun dataUri(): String {
		return String.format("data:image/%s;base64,%s", format.extension, hash())
	}

	private fun hash(): String? {
		return Base64.getEncoder().encodeToString(data)
	}
}
