package dev.capybaralabs.shipa.discord

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("discord")
data class DiscordProperties(
	var publicKey: String = "",
)
