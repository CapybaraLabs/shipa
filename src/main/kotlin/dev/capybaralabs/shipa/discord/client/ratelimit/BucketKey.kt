package dev.capybaralabs.shipa.discord.client.ratelimit

import org.springframework.http.HttpMethod

/**
 * Orignally based on [Twilight's ratelimiter implementation](https://github.com/twilight-rs/twilight/blob/23611c46f2903ff6784bc06b33cab2170719b44f/twilight-http-ratelimiting/src/request.rs#L117)
 * licensed under the [ISC license](https://github.com/twilight-rs/twilight/blob/23611c46f2903ff6784bc06b33cab2170719b44f/LICENSE.md).
 */
sealed interface BucketKey

// Operating on global commands.
data class ApplicationsCommands(val id: Long) : BucketKey

// Operating on a specific command.
data class ApplicationsCommandsId(val id: Long) : BucketKey

// Operating on commands in a guild.
data class ApplicationsGuildsCommands(val id: Long) : BucketKey

// Operating on a specific command in a guild.
data class ApplicationsGuildsCommandsId(val id: Long) : BucketKey

// Operating on a channel.
data class ChannelsId(val id: Long) : BucketKey

// Operating on a channel's followers.
data class ChannelsIdFollowers(val id: Long) : BucketKey

// Operating on a channel's invites.
data class ChannelsIdInvites(val id: Long) : BucketKey

// Operating on a channel's messages.
data class ChannelsIdMessages(val id: Long) : BucketKey

// Operating on a channel's messages by bulk deleting.
data class ChannelsIdMessagesBulkDelete(val id: Long) : BucketKey

// Operating on an individual channel's message.
data class ChannelsIdMessagesId(val method: HttpMethod, val id: Long) : BucketKey

// Crossposting an individual channel's message.
data class ChannelsIdMessagesIdCrosspost(val id: Long) : BucketKey

// Operating on an individual channel's message's reactions.
data class ChannelsIdMessagesIdReactions(val id: Long) : BucketKey

// Operating on an individual channel's message's reactions while
// specifying the user ID and emoji type.
data class ChannelsIdMessagesIdReactionsUserIdType(val id: Long) : BucketKey

// Operating on an individual channel's message's threads.
data class ChannelsIdMessagesIdThreads(val id: Long) : BucketKey

// Operating on a channel's permission overwrites by ID.
data class ChannelsIdPermissionsOverwriteId(val id: Long) : BucketKey

// Operating on a channel's pins.
data class ChannelsIdPins(val id: Long) : BucketKey

// Operating on a channel's individual pinned message.
data class ChannelsIdPinsMessageId(val id: Long) : BucketKey

// Operating on a group DM's recipients.
data class ChannelsIdRecipients(val id: Long) : BucketKey

// Operating on a thread's members.
data class ChannelsIdThreadMembers(val id: Long) : BucketKey

// Operating on a thread's member.
data class ChannelsIdThreadMembersId(val id: Long) : BucketKey

// Operating on a channel's threads.
data class ChannelsIdThreads(val id: Long) : BucketKey

// Operating on a channel's typing indicator.
data class ChannelsIdTyping(val id: Long) : BucketKey

// Operating on a channel's webhooks.
data class ChannelsIdWebhooks(val id: Long) : BucketKey

// Operating with the gateway information.
data object Gateway : BucketKey

// Operating with the gateway information tailored to the current user.
data object GatewayBot : BucketKey

// Operating on the guild resource.
data object Guilds : BucketKey

// Reading one of user's guilds.
data class GuildsIdRead(val id: Long) : BucketKey

// Modifying one of user's guilds.
data class GuildsIdModify(val id: Long) : BucketKey

// Operating on a ban from one of the user's guilds.
data class GuildsIdAuditLogs(val id: Long) : BucketKey

// Operating on a guild's auto moderation rules.
data class GuildsIdAutoModerationRules(val id: Long) : BucketKey

// Operating on an auto moderation rule from  one of the user's guilds.
data class GuildsIdAutoModerationRulesId(val id: Long) : BucketKey

// Operating on one of the user's guilds' bans.
data class GuildsIdBans(val id: Long) : BucketKey

// Operating on a ban from one of the user's guilds.
data class GuildsIdBansId(val id: Long) : BucketKey

// Operating on specific member's ban from one of the user's guilds.
data class GuildsIdBansUserId(val id: Long) : BucketKey

// Operating on one of the user's guilds' channels.
data class GuildsIdChannels(val id: Long) : BucketKey

// Operating on one of the user's guilds' emojis.
data class GuildsIdEmojis(val id: Long) : BucketKey

// Operating on an emoji from one of the user's guilds.
data class GuildsIdEmojisId(val id: Long) : BucketKey

// Operating on one of the user's guilds' integrations.
data class GuildsIdIntegrations(val id: Long) : BucketKey

// Operating on an integration from one of the user's guilds.
data class GuildsIdIntegrationsId(val id: Long) : BucketKey

// Operating on an integration from one of the user's guilds by synchronizing it.
data class GuildsIdIntegrationsIdSync(val id: Long) : BucketKey

// Operating on one of the user's guilds' invites.
data class GuildsIdInvites(val id: Long) : BucketKey

// Operating on one of the user's guilds' members.
data class GuildsIdMembers(val id: Long) : BucketKey

// Operating on a member from one of the user's guilds.
data class GuildsIdMembersId(val id: Long) : BucketKey

// Operating on a role of a member from one of the user's guilds.
data class GuildsIdMembersIdRolesId(val id: Long) : BucketKey

// Operating on the user's nickname in one of the user's guilds.
data class GuildsIdMembersMeNick(val id: Long) : BucketKey

// Operating on one of the user's guilds' members by searching.
data class GuildsIdMembersSearch(val id: Long) : BucketKey

// Operating on one of the user's guilds' MFA level.
data class GuildsIdMfa(val id: Long) : BucketKey

// Operating on one of the user's guilds' by previewing it.
data class GuildsIdPreview(val id: Long) : BucketKey

// Operating on one of the user's guilds' by pruning members.
data class GuildsIdPrune(val id: Long) : BucketKey

// Operating on the voice regions of one of the user's guilds.
data class GuildsIdRegions(val id: Long) : BucketKey

// Operating on the roles of one of the user's guilds.
data class GuildsIdRoles(val id: Long) : BucketKey

// Operating on a role of one of the user's guilds.
data class GuildsIdRolesId(val id: Long) : BucketKey

// Operating on the guild's scheduled events.
data class GuildsIdScheduledEvents(val id: Long) : BucketKey

// Operating on a particular guild's scheduled events.
data class GuildsIdScheduledEventsId(val id: Long) : BucketKey

// Operating on a particular guild's scheduled event users.
data class GuildsIdScheduledEventsIdUsers(val id: Long) : BucketKey

// Operating on one of the user's guilds' stickers.
data class GuildsIdStickers(val id: Long) : BucketKey

// Operating on one of the user's guilds' templates.
data class GuildsIdTemplates(val id: Long) : BucketKey

// Operating on a template from one of the user's guilds.
data class GuildsIdTemplatesCode(val id: Long, val code: String) : BucketKey

// Operating on one of the user's guilds' threads.
data class GuildsIdThreads(val id: Long) : BucketKey

// Operating on one of the user's guilds' vanity URL.
data class GuildsIdVanityUrl(val id: Long) : BucketKey

// Operating on one of the user's guilds' voice states.
data class GuildsIdVoiceStates(val id: Long) : BucketKey

// Operating on one of the user's guilds' webhooks.
data class GuildsIdWebhooks(val id: Long) : BucketKey

// Operating on one of the user's guilds' welcome screen.
data class GuildsIdWelcomeScreen(val id: Long) : BucketKey

// Operating on one of the user's guild's widget settings.
data class GuildsIdWidget(val id: Long) : BucketKey

// Operating on one of the user's guild's widget.
data class GuildsIdWidgetJson(val id: Long) : BucketKey

// Operating on a guild template.
data class GuildsTemplatesCode(val code: String) : BucketKey

// Operating on an interaction's callback.
//
// This path is not bound to the application's global rate limit.
data class InteractionCallback(val id: Long) : BucketKey

// Operating on an invite.
data object InvitesCode : BucketKey

// Operating on the user's application information.
data object OAuthApplicationsMe : BucketKey

// Operating on the current authorization's information.
data object OAuthMe : BucketKey

// Operating on stage instances.
data object StageInstances : BucketKey

// Operating on sticker packs.
data object StickerPacks : BucketKey

// Operating on a sticker.
data object Stickers : BucketKey

// Reading the current authorization's user.
data object UsersMeRead : BucketKey

// Modifying the current authorization's user.
data object UsersMeModify : BucketKey

// Operating on a user.
data object UsersId : BucketKey

// Operating on the user's private channels.
data object UsersIdChannels : BucketKey

// Operating on the user's connections.
data object UsersIdConnections : BucketKey

// Operating on the state of a guild that the user is in.
data object UsersIdGuilds : BucketKey

// Operating on the state of a guild that the user is in.
data object UsersIdGuildsId : BucketKey

// Operating on the state of a guild that the user, as a member, is in.
data object UsersIdGuildsIdMember : BucketKey

// Operating on the voice regions available to the current user.
data object VoiceRegions : BucketKey

// Operating on a webhook as a bot.
data class WebhooksId(val id: Long) : BucketKey

// Operating on a webhook as a webhook.
//
// When used with interactions, this path is not bound to the application's
// global rate limit.
data class WebhooksIdToken(val id: Long, val token: String) : BucketKey

// Operating on a message created by a webhook.
//
// When used with interactions, this path is not bound to the application's
// global rate limit.
data class WebhooksIdTokenMessagesId(val id: Long, val token: String) : BucketKey
