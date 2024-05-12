package dev.capybaralabs.shipa.discord.oauth2

import dev.capybaralabs.shipa.discord.client.DiscordAuthToken

/**
 * The used token is lacking a scope to access the requested Discord resource.
 */
class OAuth2ScopeException(
	val token: DiscordAuthToken.Oauth2,
	val missingScope: OAuth2Scope,
) : RuntimeException(
	"Token is missing scope $missingScope, has scopes ${token.scopes}",
)
