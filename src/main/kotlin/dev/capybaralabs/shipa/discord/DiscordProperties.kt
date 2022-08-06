package dev.capybaralabs.shipa.discord

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("shipa.discord")
data class DiscordProperties(
	var publicKey: String = "",
	var applicationId: Long = 0,
	var botToken: String = "",
)
