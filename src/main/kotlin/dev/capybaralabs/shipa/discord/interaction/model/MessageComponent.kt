package dev.capybaralabs.shipa.discord.interaction.model

import com.fasterxml.jackson.annotation.JsonValue
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.Button.ButtonStyle.LINK
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ComponentType.ACTION_ROW
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ComponentType.BUTTON
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ComponentType.SELECT_MENU
import dev.capybaralabs.shipa.discord.interaction.model.MessageComponent.ComponentType.TEXT_INPUT
import java.util.Optional
import org.springframework.util.Assert

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

	sealed interface NotActionRow : MessageComponent

	data class ActionRow internal constructor(
		val components: List<NotActionRow>,
	) : MessageComponent {

		override val type = ACTION_ROW

		companion object {
			fun selectMenu(selectMenu: SelectMenu): ActionRow {
				return ActionRow(listOf(selectMenu))
			}

			fun buttons(vararg buttons: Button): ActionRow {
				return buttons(listOf(*buttons))
			}

			fun buttons(buttons: List<Button>): ActionRow {
				Assert.isTrue(buttons.size <= 5, "Only a maximum of 5 buttons is allowed in an ActionRow")
				return ActionRow(buttons)
			}
		}
	}

	data class ModalActionRow internal constructor(
		val components: List<TextInput>,
	) : MessageComponent {

		override val type = ACTION_ROW

		companion object {
			fun textInputs(inputs: List<TextInput>): ModalActionRow {
				Assert.isTrue(inputs.isNotEmpty(), "At least one text input is required")
				Assert.isTrue(inputs.size <= 5, "Only a maximum of 5 text inputs is allowed in a Modal ActionRow")
				return ModalActionRow(inputs)
			}
		}
	}


	data class PartialEmoji(
		val id: Optional<Long> = Optional.empty(),
		val name: Optional<String> = Optional.empty(),
		val animated: Boolean? = null,
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
	) : NotActionRow {

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
			override val customId: String,
			override val label: String? = null,
			override val emoji: PartialEmoji? = null,
			override val disabled: Boolean? = null,
		) : Button(style, label, emoji, customId, null, disabled)


		data class Link(
			override val url: String,
			override val label: String? = null,
			override val emoji: PartialEmoji? = null,
			override val disabled: Boolean? = null,
		) : Button(LINK, label, emoji, null, url, disabled)

	}


	/**
	 * [Discord Message Component Select Menu](https://discord.com/developers/docs/interactions/message-components#select-menu-object)
	 */
	data class SelectMenu(
		val customId: String,
		val options: List<SelectOption>,
		val placeholder: String? = null,
		val minValues: Int? = null,
		val maxValues: Int? = null,
		val disabled: Boolean? = null,
	) : NotActionRow {

		override val type = SELECT_MENU

		data class SelectOption(
			val label: String,
			val value: String,
			val description: String? = null,
			val emoji: PartialEmoji? = null,
			val default: Boolean? = null,
		)
	}

	/**
	 * [Discord Message Component Text Input](https://discord.com/developers/docs/interactions/message-components#text-inputs-text-input-structure)
	 */
	data class TextInput(
		val customId: String,
		val style: TextInputStyle,
		val label: String,
		val minLength: Int? = null,
		val maxLength: Int? = null,
		val required: Boolean? = null,
		val value: String? = null,
		val placeholder: String? = null,
	) : NotActionRow {

		override val type = TEXT_INPUT

		enum class TextInputStyle(@JsonValue val value: Int) {
			SHORT(1),
			PARAGRAPH(2),
		}

	}

}
