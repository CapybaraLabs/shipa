package dev.capybaralabs.shipa.discord.interaction

import dev.capybaralabs.shipa.ApplicationTest
import dev.capybaralabs.shipa.discord.interaction.command.ApplicationCommandService
import dev.capybaralabs.shipa.discord.interaction.command.CommandLookupService
import dev.capybaralabs.shipa.discord.interaction.command.InteractionCommand
import dev.capybaralabs.shipa.discord.interaction.model.ApplicationCommandType.CHAT_INPUT
import dev.capybaralabs.shipa.discord.interaction.model.InteractionData.ApplicationCommandData
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.InteractionWithData.ApplicationCommand
import dev.capybaralabs.shipa.discord.interaction.model.InteractionObject.ShipaMetadata
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand
import dev.capybaralabs.shipa.discord.interaction.model.create.CreateCommand.CreateSlashGlobalCommand
import dev.capybaralabs.shipa.discord.model.DiscordLocale.GREEK
import java.time.Instant
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

internal class ApplicationCommandServiceTest : ApplicationTest() {

	@MockitoSpyBean
	private lateinit var service: ApplicationCommandService

	@MockitoSpyBean
	private lateinit var commandLookupService: CommandLookupService

	@Autowired
	private lateinit var repo: InteractionRepository

	@Test
	internal fun whenOnInteraction_saveInteraction() {
		runBlocking {
			val interaction = ApplicationCommand(
				42, ShipaMetadata(Instant.now()), 42, "foo", 1, GREEK,
				ApplicationCommandData(42, "bar", CHAT_INPUT, null, null, null, null), null, 69, null, null, null, null, null,
			)
			whenever(commandLookupService.findByName(interaction.data.name)).thenReturn(
				object : InteractionCommand {
					override fun creation(): CreateCommand {
						return CreateSlashGlobalCommand("test", "Test command")
					}
				},
			)

			assertThat(repo.find(42)).isNull()

			service.onInteraction(interaction, InitialResponse(CompletableDeferred(), CompletableFuture()))

			assertThat(repo.find(42))
				.isNotNull
				.isEqualTo(interaction)
		}
	}
}
