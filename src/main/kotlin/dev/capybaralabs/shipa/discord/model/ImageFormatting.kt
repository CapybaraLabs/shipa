package dev.capybaralabs.shipa.discord.model

/**
 * [Discord Image Formatting](https://discord.com/developers/docs/reference#image-formatting)
 */
object ImageFormatting {

	const val BASE_URL = "https://cdn.discordapp.com"
	const val URL_FORMAT = "$BASE_URL%s.%s"

	enum class Format(vararg val extension: String) {
		JPEG("jpg", "jpeg"),
		PNG("png"),
		WEBP("webp"),
		GIF("gif"),
		LOTTIE("json"),
	}

	fun imageUrl(path: String, format: Format = Format.PNG): String {
		return String.format(URL_FORMAT, path, format.extension[0])
	}

}
