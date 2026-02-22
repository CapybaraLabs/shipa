package dev.capybaralabs.shipa

import dev.capybaralabs.shipa.discord.ShipaDiscordProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan

@ComponentScan
@EnableConfigurationProperties(ShipaDiscordProperties::class)
class ShipaModule
