package dev.capybaralabs.shipa.discord.gateway

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan

@ComponentScan
@EnableConfigurationProperties(DiscordGatewayProperties::class)
class ShipaGatewayModule
