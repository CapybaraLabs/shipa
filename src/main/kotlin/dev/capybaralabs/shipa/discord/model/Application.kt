package dev.capybaralabs.shipa.discord.model

import dev.capybaralabs.shipa.discord.model.ImageFormatting.Format.PNG
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/**
 * [Discord Application](https://discord.com/developers/docs/resources/application#application-object)
 */
data class Application(
	val id: Long,
	val name: String,
	val icon: Optional<String>,
	val description: String,
	val rpcOrigins: List<String>?,
	val botPublic: Boolean,
	val botRequireCodeGrant: Boolean,
	val termsOfServiceUrl: String?,
	val privacyPolicyUrl: String?,
	val owner: User, // can it really be null?
	val verifyKey: String,
//	val team: Optional<Team>,
	val guildId: Long?,
//	val guild: PartialGuild?,
	val primarySkuId: Long?,
	val slug: String?,
	val coverImage: String?,
	val flags: IntBitfield<ApplicationFlag>?,
	val approximateGuildCount: Int?,
	val tags: List<String>?,
//	val installParams: InstallParams?,
	val customInstallUrl: String?,
	val roleConnectionsVerificationUrl: String?,
) {

	fun iconUrl(): String? {
		return icon.getOrNull()?.let {
			ImageFormatting.imageUrl("/app-icons/$id/$it", PNG)
		}
	}

	fun coverUrl(): String? {
		return coverImage?.let {
			ImageFormatting.imageUrl("/app-icons/$id/$it", PNG)
		}
	}
}

/**
 * [Application Flags](https://discord.com/developers/docs/resources/application#application-object-application-flags)
 */
enum class ApplicationFlag(override val value: Int) : IntBitflag {
	APPLICATION_AUTO_MODERATION_RULE_CREATE_BADGE(1 shl 6),
	GATEWAY_PRESENCE(1 shl 12),
	GATEWAY_PRESENCE_LIMITED(1 shl 13),
	GATEWAY_GUILD_MEMBERS(1 shl 14),
	GATEWAY_GUILD_MEMBERS_LIMITED(1 shl 15),
	VERIFICATION_PENDING_GUILD_LIMIT(1 shl 16),
	EMBEDDED(1 shl 17),
	GATEWAY_MESSAGE_CONTENT(1 shl 18),
	GATEWAY_MESSAGE_CONTENT_LIMITED(1 shl 19),
	APPLICATION_COMMAND_BADGE(1 shl 23),
}
