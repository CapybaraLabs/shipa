package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.discord.interaction.InitialResponse
import dev.capybaralabs.shipa.discord.interaction.InteractionRepository
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionService
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.timeSuspending
import dev.capybaralabs.shipa.logger
import org.springframework.stereotype.Service

interface ApplicationCommandService {

	suspend fun onInteraction(interaction: InteractionWithData, initialResponse: InitialResponse)
}


@Service
private class ApplicationCommandServiceImpl(
	private val commandLookupService: CommandLookupService,
	private val interactionRepository: InteractionRepository,
	private val metrics: ShipaMetrics,
	private val unifiedInteractionService: UnifiedInteractionService,
) : ApplicationCommandService {

	override suspend fun onInteraction(interaction: InteractionWithData, initialResponse: InitialResponse) {
		logger().trace("Interaction {}: Looking up command", interaction.id)
		val command = when (interaction) {
			is InteractionWithData.ApplicationCommand -> commandLookupService.findByName(interaction.data.name)
			is InteractionWithData.MessageComponent -> commandLookupService.findByCustomId(interaction.data.customId)
			is InteractionWithData.Autocomplete -> commandLookupService.findByName(interaction.data.name)
			is InteractionWithData.ModalSubmit -> commandLookupService.findByCustomId(interaction.data.customId)
		}
		logger().trace("Interaction {}: Looked up command", interaction.id)

		if (command == null) {
			logger().warn("Unknown Command {}", interaction)
			return
		}

		logger().trace("Interaction {}: Creating state", interaction.id)
		val stateHolder = unifiedInteractionService.create(interaction, command.autoAckTactic(), initialResponse)

		//should be below state creation (and therefore start of auto-ack behaviour) because redis may hang sometimes
		logger().trace("Interaction {}: Saving", interaction.id)
		interactionRepository.save(interaction)
		logger().trace("Interaction {}: Saved", interaction.id)

		logger().trace("Interaction {}: Processing", interaction.id)
		metrics.commandProcessTime(command.name(), interaction.type.name).timeSuspending {
			command.onInteraction(stateHolder)
		}
		logger().trace("Interaction {}: Done", interaction.id)
	}

}
