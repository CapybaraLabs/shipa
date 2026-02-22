package dev.capybaralabs.shipa.discord

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("shipa.discord")
data class ShipaDiscordProperties(
	var publicKey: String = "",
	var applicationId: Long = 0,
	var botToken: String = "",
	var discordApiRootUrl: String = "https://discord.com/api/v10/",
)
