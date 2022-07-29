package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonValue
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ComponentType.ACTION_ROW
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ComponentType.BUTTON
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ComponentType.SELECT_MENU
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ComponentType.TEXT_INPUT
import java.util.Optional

/**
 * [Discord Message Component](https://discord.com/developers/docs/interactions/message-components)
 */
sealed interface MessageComponent {
	val type: ComponentType

	enum class ComponentType(@JsonValue val value: Int) {
		ACTION_ROW(1),
		BUTTON(2),
		SELECT_MENU(3),
		TEXT_INPUT(4),
	}

	data class ActionRow(
		val components: List<MessageComponent>,
	) : MessageComponent {

		override val type = ACTION_ROW
	}


	data class PartialEmoji(
		val id: Optional<Long>,
		val name: Optional<String>,
		val animated: Boolean?,
	)

	/**
	 * [Discord Message Component Button](https://discord.com/developers/docs/interactions/message-components#button-object)
	 */
	abstract class Button(
		open val style: ButtonStyle,
		open val label: String?,
		open val emoji: PartialEmoji?,
		open val customId: String?,
		open val url: String?,
		open val disabled: Boolean?,
	) : MessageComponent {

		override val type = BUTTON

		/**
		 * [Discord Message Component Button Style](https://discord.com/developers/docs/interactions/message-components#button-object-button-styles)
		 */
		enum class ButtonStyle(@JsonValue val value: Int) {
			PRIMARY(1),
			SECONDARY(2),
			SUCCESS(3),
			DANGER(4),
			LINK(5),
		}


		data class Regular(
			override val style: ButtonStyle,
			override val label: String?,
			override val emoji: PartialEmoji?,
			override val customId: String,
			override val disabled: Boolean?,
		) : Button(style, label, emoji, customId, null, disabled)


		data class Link(
			override val style: ButtonStyle,
			override val label: String?,
			override val emoji: PartialEmoji?,
			override val url: String,
			override val disabled: Boolean?,
		) : Button(style, label, emoji, null, url, disabled)

	}


	/**
	 * [Discord Message Component Select Menu](https://discord.com/developers/docs/interactions/message-components#select-menu-object)
	 */
	data class SelectMenu(
		val customId: String,
		val options: List<SelectOption>,
		val placeholder: String?,
		val minValues: Int?,
		val maxValues: Int?,
		val disabled: Boolean?,
	) : MessageComponent {

		override val type = SELECT_MENU

		data class SelectOption(
			val label: String,
			val value: String,
			val description: String?,
			val emojis: PartialEmoji?,
			val default: Boolean?,
		)
	}

	/**
	 * [Discord Message Component Text Input](https://discord.com/developers/docs/interactions/message-components#text-inputs-text-input-structure)
	 */
	data class TextInput(
		val customId: String,
		val style: TextInputStyle,
		val label: String,
		val minLength: Int?,
		val maxLength: Int?,
		val required: Boolean?,
		val value: String?,
		val placeholder: String?,
	) : MessageComponent {

		override val type = TEXT_INPUT

		enum class TextInputStyle(@JsonValue val value: Int) {
			SHORT(1),
			PARAGRAPH(2),
		}

	}

}
