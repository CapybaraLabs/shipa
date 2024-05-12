package dev.capybaralabs.shipa.discord.model

import dev.capybaralabs.shipa.discord.model.ImageFormatting.Format.GIF
import dev.capybaralabs.shipa.discord.model.ImageFormatting.Format.PNG
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

interface HasGuildIcon {
	val id: Long
	val icon: Optional<String>

	fun iconUrl(): String? {
		return icon.getOrNull()?.let {
			val format = if (it.startsWith("a_")) GIF else PNG
			ImageFormatting.imageUrl("/icons/$id/$it", format)
		}
	}

}
