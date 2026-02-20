package administration.admin.initializesettings

import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.common.CommandException
import administration.common.SettingsConstants
import administration.domain.SettingsAggregate
import administration.events.SettingsCreatedEvent
import administration.support.metadata.AdminSecurityHeaders
import java.util.UUID
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InitializeSettingssettingsIdissetonceTest {

  private lateinit var fixture: FixtureConfiguration<SettingsAggregate>

  private val testConnectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")

  // Define standard test metadata
  private val testMetaData = MetaData.with(AdminSecurityHeaders.SESSION_ID, "test-session-123")

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(SettingsAggregate::class.java)
  }

  @Test
  fun `should create SettingsAggregate successfully`() {
    val command =
            CreateSettingsCommand(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = testConnectionId
            )

    val commandMessage =
            GenericCommandMessage.asCommandMessage<CreateSettingsCommand>(command)
                    .withMetaData(testMetaData)

    val expectedEvent =
            SettingsCreatedEvent(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = testConnectionId
            )

    fixture.givenNoPriorActivity()
            .`when`(commandMessage)
            .expectSuccessfulHandlerExecution()
            .expectEvents(expectedEvent)
  }

  @Test
  fun `should ignore duplicate creation with same SETTINGS_ID`() {
    val command =
            CreateSettingsCommand(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = testConnectionId
            )

    val commandMessage =
            GenericCommandMessage.asCommandMessage<CreateSettingsCommand>(command)
                    .withMetaData(testMetaData)

    val existingEvent =
            SettingsCreatedEvent(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = testConnectionId
            )

    // GIVEN aggregate already created
    fixture.given(existingEvent)
            .`when`(commandMessage)
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
  }

  @Test
  fun `should fail on creation with wrong settingsId`() {
    val wrongId = UUID.randomUUID()
    val command = CreateSettingsCommand(settingsId = wrongId, connectionId = testConnectionId)

    val commandMessage =
            GenericCommandMessage.asCommandMessage<CreateSettingsCommand>(command)
                    .withMetaData(testMetaData)

    val existingEvent =
            SettingsCreatedEvent(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = testConnectionId
            )

    // GIVEN aggregate already created with correct SETTINGS_ID
    fixture.given(existingEvent)
            .`when`(commandMessage)
            .expectException(CommandException::class.java)
  }
}
