package dev.capybaralabs.shipa.discord.interaction.model

import dev.capybaralabs.shipa.discord.interaction.model.InteractionData.ApplicationCommandData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionData.MessageComponentData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionData.ModalSubmitData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.Ping
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.ShipaMetadata
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.APPLICATION_COMMAND
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.MESSAGE_COMPONENT
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.MODAL_SUBMIT
import dev.capybaralabs.shipa.discord.interaction.model.InteractionType.PING
import dev.capybaralabs.shipa.discord.model.DiscordLocale
import dev.capybaralabs.shipa.discord.model.InteractionMember
import dev.capybaralabs.shipa.discord.model.Message
import dev.capybaralabs.shipa.discord.model.Permission
import dev.capybaralabs.shipa.discord.model.StringBitfield
import dev.capybaralabs.shipa.discord.model.User
import java.time.Instant

/**
 * [Discord Interaction](https://discord.com/developers/docs/interactions/receiving-and-responding#interaction-object)
 */
sealed interface InteractionObject {
	val id: Long
	val shipaMetadata: ShipaMetadata
	val applicationId: Long
	val token: String
	val type: InteractionType
	val version: Int
	val locale: DiscordLocale?
	val data: InteractionData?
	val guildId: Long?
	val channelId: Long?
	val member: InteractionMember?
	val user: User?
	val message: Message?
	val appPermissions: StringBitfield<Permission>?
	val guildLocale: DiscordLocale?

	data class ShipaMetadata(
		val received: Instant,
	)

	data class Ping(
		override val id: Long,
		override val shipaMetadata: ShipaMetadata,
		override val applicationId: Long,
		override val token: String,
		override val version: Int,
	) : InteractionObject {
		override val type = PING
		override val locale: Nothing? = null
		override val data: Nothing? = null
		override val guildId: Nothing? = null
		override val channelId: Nothing? = null
		override val member: Nothing? = null
		override val user: Nothing? = null
		override val message: Nothing? = null
		override val appPermissions: Nothing? = null
		override val guildLocale: Nothing? = null
	}

	sealed interface InteractionWithData : InteractionObject {
		override val data: InteractionData
		override val locale: DiscordLocale

		fun invoker(): User {
			return user ?: member!!.user
		}

		data class ApplicationCommand(
			override val id: Long,
			override val shipaMetadata: ShipaMetadata,
			override val applicationId: Long,
			override val token: String,
			override val version: Int,
			override val locale: DiscordLocale,
			override val data: ApplicationCommandData,
			override val guildId: Long?,
			override val channelId: Long,
			override val member: InteractionMember?,
			override val user: User?,
			override val message: Message?,
			override val appPermissions: StringBitfield<Permission>?,
			override val guildLocale: DiscordLocale?,
		) : InteractionWithData {
			override val type = APPLICATION_COMMAND
		}

		data class MessageComponent(
			override val id: Long,
			override val shipaMetadata: ShipaMetadata,
			override val applicationId: Long,
			override val token: String,
			override val version: Int,
			override val locale: DiscordLocale,
			override val data: MessageComponentData,
			override val guildId: Long?,
			override val channelId: Long,
			override val member: InteractionMember?,
			override val user: User?,
			override val message: Message,
			override val appPermissions: StringBitfield<Permission>?,
			override val guildLocale: DiscordLocale?,
		) : InteractionWithData {
			override val type = MESSAGE_COMPONENT
		}

		data class Autocomplete(
			override val id: Long,
			override val shipaMetadata: ShipaMetadata,
			override val applicationId: Long,
			override val token: String,
			override val version: Int,
			override val locale: DiscordLocale,
			override val data: ApplicationCommandData,
			override val guildId: Long?,
			override val channelId: Long?,
			override val member: InteractionMember?,
			override val user: User?,
			override val message: Message?,
			override val appPermissions: StringBitfield<Permission>?,
			override val guildLocale: DiscordLocale?,
		) : InteractionWithData {
			override val type = APPLICATION_COMMAND_AUTOCOMPLETE
		}

		data class ModalSubmit(
			override val id: Long,
			override val shipaMetadata: ShipaMetadata,
			override val applicationId: Long,
			override val token: String,
			override val version: Int,
			override val locale: DiscordLocale,
			override val data: ModalSubmitData,
			override val guildId: Long?,
			override val channelId: Long?,
			override val member: InteractionMember?,
			override val user: User?,
			override val message: Message?,
			override val appPermissions: StringBitfield<Permission>?,
			override val guildLocale: DiscordLocale?,
		) : InteractionWithData {
			override val type = MODAL_SUBMIT
		}

	}
}

data class UntypedInteractionObject(
	val id: Long,
	val applicationId: Long,
	val token: String,
	val version: Int,
	val type: InteractionType,
	val locale: DiscordLocale?,
	val data: InteractionData?,
	val guildId: Long?,
	val channelId: Long?,
	val member: InteractionMember?,
	val user: User?,
	val message: Message?,
	val appPermissions: StringBitfield<Permission>?,
	val guildLocale: DiscordLocale?,
) {

	fun typed(
		shipaMetadata: ShipaMetadata,
	): InteractionObject {
		return when (type) {
			PING -> Ping(id, shipaMetadata, applicationId, token, version)
			APPLICATION_COMMAND -> InteractionWithData.ApplicationCommand(
				id,
				shipaMetadata,
				applicationId,
				token,
				version,
				locale!!,
				data!! as ApplicationCommandData,
				guildId,
				channelId!!,
				member,
				user,
				message,
				appPermissions,
				guildLocale,
			)

			MESSAGE_COMPONENT -> InteractionWithData.MessageComponent(
				id,
				shipaMetadata,
				applicationId,
				token,
				version,
				locale!!,
				data!! as MessageComponentData,
				guildId,
				channelId!!,
				member,
				user,
				message!!,
				appPermissions,
				guildLocale,
			)

			APPLICATION_COMMAND_AUTOCOMPLETE -> InteractionWithData.Autocomplete(
				id,
				shipaMetadata,
				applicationId,
				token,
				version,
				locale!!,
				data!! as ApplicationCommandData,
				guildId,
				channelId,
				member,
				user,
				message,
				appPermissions,
				guildLocale,
			)

			MODAL_SUBMIT -> InteractionWithData.ModalSubmit(
				id,
				shipaMetadata,
				applicationId,
				token,
				version,
				locale!!,
				data!! as ModalSubmitData,
				guildId,
				channelId,
				member,
				user,
				message,
				appPermissions,
				guildLocale,
			)
		}
	}
}
