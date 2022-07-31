package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.DiscordProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan

@ComponentScan
@EnableConfigurationProperties(DiscordProperties::class)
class ShipaModule
