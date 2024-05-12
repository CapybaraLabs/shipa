package dev.capybaralabs.shipa.discord.oauth2


/**
 * See [Discord OAuth2 Scopes](https://discord.com/developers/docs/topics/oauth2#shared-resources-oauth2-scopes) for the full list.
 */
enum class OAuth2Scope(val discordName: String) {

	IDENTIFY("identify"),
	GUILDS("guilds"),
	;

	companion object {
		fun parse(input: String): OAuth2Scope? {
			for (scope in entries) {
				if (scope.discordName.equals(input, ignoreCase = true)) {
					return scope
				}
				if (scope.name.equals(input, ignoreCase = true)) {
					return scope
				}
			}
			return null
		}
	}
}
