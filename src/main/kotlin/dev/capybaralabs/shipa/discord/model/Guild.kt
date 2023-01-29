package dev.capybaralabs.shipa.discord.model

import com.fasterxml.jackson.annotation.JsonValue
import java.util.Optional

/**
 * [Discord Guild Object](https://discord.com/developers/docs/resources/guild#guild-object)
 */
data class Guild(
	val id: Long,
	val name: String,
	val icon: Optional<String>,
	val iconHash: Optional<String>?,
	val splash: Optional<String>,
	val discoverySplash: Optional<String>,
	val owner: Boolean?,
	val ownerId: Long,
	val permissions: StringBitfield<Permission>?,
	val afkChannelId: Optional<Long>,
	val afkTimeout: Int,
	val widgetEnabled: Boolean?,
	val widgetChannelId: Optional<Long>?,
	val verificationLevel: VerificationLevel,
	val defaultMessageNotifications: DefaultMessageNotificationLevel,
	val explicitContentFilter: ExplicitContentFilterLevel,
	val roles: List<Role>,
//	val emojis: List<Emoji>,
	val features: List<GuildFeature>,
	val mfaLevel: MfaLevel?,
	val applicationId: Optional<Long>,
	val systemChannelId: Optional<Long>,
	val systemChannelFlags: IntBitfield<SystemChannelFlag>,
	val rulesChannelId: Optional<Long>,
	val maxPresences: Optional<Int>?,
	val maxMembers: Int?,
	val vanityUrlCode: Optional<String>,
	val description: Optional<String>,
	val banner: Optional<String>,
	val premiumTier: PremiumTier,
	val premiumSubscriptionCount: Int?,
	val preferredLocale: DiscordLocale,
	val publicUpdatesChannelId: Optional<String>,
	val maxVideoChannelUsers: Int?,
	val approximateMemberCount: Int?,
//	val welcomeScreen: WelcomeScreen?,
	val nsfwLevel: GuildNsfwLevel,
//	val stickers: List<Sticker>,
	val premiumProgressBarEnabled: Boolean,
)

/**
 * [Default Message Notification Level](https://discord.com/developers/docs/resources/guild#guild-object-default-message-notification-level)
 */
enum class DefaultMessageNotificationLevel(@JsonValue val value: Int) {
	ALL_MESSAGES(0),
	ONLY_MENTIONS(1),
}

/**
 * [Explicit Content Filter Level](https://discord.com/developers/docs/resources/guild#guild-object-explicit-content-filter-level)
 */
enum class ExplicitContentFilterLevel(@JsonValue val value: Int) {
	DISABLED(0),
	MEMBERS_WITHOUT_ROLES(1),
	ALL_MEMBERS(2),
}

/**
 * [MFA Level](https://discord.com/developers/docs/resources/guild#guild-object-mfa-level)
 */
enum class MfaLevel(@JsonValue val value: Int) {
	NONE(0),
	ELEVATED(1),
}

/**
 * [Verification Level](https://discord.com/developers/docs/resources/guild#guild-object-verification-level)
 */
enum class VerificationLevel(@JsonValue val value: Int) {
	NONE(0),
	LOW(1),
	MEDIUM(2),
	HIGH(3),
	VERY_HIGH(4),
}

/**
 * [Guild NSFW Level](https://discord.com/developers/docs/resources/guild#guild-object-guild-nsfw-level)
 */
enum class GuildNsfwLevel(@JsonValue val value: Int) {
	DEFAULT(0),
	EXPLICIT(1),
	SAFE(2),
	AGE_RESTRICTED(3),
}

/**
 * [Premium Tier](https://discord.com/developers/docs/resources/guild#guild-object-premium-tier)
 */
enum class PremiumTier(@JsonValue val value: Int) {
	NONE(0),
	TIER_1(1),
	TIER_2(2),
	TIER_3(3),
}

/**
 * [System Channel Flags](https://discord.com/developers/docs/resources/guild#guild-object-system-channel-flags)
 */
enum class SystemChannelFlag(override val value: Int) : IntBitflag {
	SUPPRESS_JOIN_NOTIFICATIONS(1 shl 0),
	SUPPRESS_PREMIUM_SUBSCRIPTIONS(1 shl 1),
	SUPPRESS_GUILD_REMINDER_NOTIFICATIONS(1 shl 2),
	SUPPRESS_JOIN_NOTIFICATION_REPLIES(1 shl 3),
}

/**
 * [Guild Features](https://discord.com/developers/docs/resources/guild#guild-object-guild-features)
 */
enum class GuildFeature {
	ANIMATED_BANNER,
	ANIMATED_ICON,
	AUTO_MODERATION,
	BANNER,
	COMMUNITY,
	DISCOVERABLE,
	FEATURABLE,
	INVITE_SPLASH,
	MEMBER_VERIFICATION_GATE_ENABLED,
	MONETIZATION_ENABLED,
	MORE_STICKERS,
	NEWS,
	PARTNERED,
	PREVIEW_ENABLED,
	PRIVATE_THREADS,
	ROLE_ICONS,
	TICKETED_EVENTS_ENABLED,
	VANITY_URL,
	VERIFIED,
	VIP_REGIONS,
	WELCOME_SCREEN_ENABLED,
	EXPOSED_TO_ACTIVITIES_WTP_EXPERIMENT,
}
