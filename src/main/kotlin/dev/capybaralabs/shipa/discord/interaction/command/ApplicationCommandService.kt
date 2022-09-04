package dev.capybaralabs.shipa.discord.interaction.command

import dev.capybaralabs.shipa.ShipaMetrics
import dev.capybaralabs.shipa.discord.interaction.InteractionRepository
import dev.capybaralabs.shipa.discord.interaction.UnifiedInteractionService
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionResponse
import dev.capybaralabs.shipa.logger
import kotlinx.coroutines.CompletableDeferred
import org.springframework.stereotype.Service

interface ApplicationCommandService {

	suspend fun onInteraction(interaction: InteractionWithData, result: CompletableDeferred<InteractionResponse>)
}


@Service
private class ApplicationCommandServiceImpl(
	private val commandLookupService: CommandLookupService,
	private val interactionRepository: InteractionRepository,
	private val metrics: ShipaMetrics,
	private val unifiedInteractionService: UnifiedInteractionService,
) : ApplicationCommandService {

	override suspend fun onInteraction(interaction: InteractionWithData, result: CompletableDeferred<InteractionResponse>) {
		interactionRepository.save(interaction)

		val command = when (interaction) {
			is InteractionWithData.ApplicationCommand -> commandLookupService.findByName(interaction.data.name)
			is InteractionWithData.MessageComponent -> commandLookupService.findByCustomId(interaction.data.customId)
			is InteractionWithData.Autocomplete -> commandLookupService.findByName(interaction.data.name)
			is InteractionWithData.ModalSubmit -> commandLookupService.findByCustomId(interaction.data.customId)
		}

		if (command == null) {
			logger().warn("Unknown Command {}", interaction)
			return
		}

		val stateHolder = unifiedInteractionService.create(interaction, command.autoAckTactic(), result)
		metrics.commandProcessTime.labels(command.name(), interaction.type.name).startTimer().use {
			command.onInteraction(stateHolder)
		}
	}

}
