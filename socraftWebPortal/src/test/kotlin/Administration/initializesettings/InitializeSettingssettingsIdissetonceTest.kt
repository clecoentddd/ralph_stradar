package administration.initializesettings

import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.common.CommandException
import administration.common.SettingsConstants
import administration.domain.SettingsAggregate
import administration.events.SettingsCreatedEvent
import java.util.UUID
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InitializeSettingssettingsIdissetonceTest {

  private lateinit var fixture: FixtureConfiguration<SettingsAggregate>

  private val testConnectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")

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

    val expectedEvent =
            SettingsCreatedEvent(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = testConnectionId
            )

    fixture.givenNoPriorActivity()
            .`when`(command)
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

    val expectedEvent =
            SettingsCreatedEvent(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = testConnectionId
            )

    // GIVEN aggregate already created
    fixture.given(expectedEvent)
            .`when`(command)
            .expectSuccessfulHandlerExecution()
            .expectNoEvents() // duplicate creation does not emit new event
  }

  @Test
  fun `should fail on creation with wrong settingsId`() {
    val wrongId = UUID.randomUUID()
    val command = CreateSettingsCommand(settingsId = wrongId, connectionId = testConnectionId)

    val expectedEvent =
            SettingsCreatedEvent(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = testConnectionId
            )

    // GIVEN aggregate already created with correct SETTINGS_ID
    fixture.given(expectedEvent).`when`(command).expectException(CommandException::class.java)
  }
}
