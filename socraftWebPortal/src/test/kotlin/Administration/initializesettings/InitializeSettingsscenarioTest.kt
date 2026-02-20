package administration.initializesettings

import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.common.SettingsConstants
import administration.domain.SettingsAggregate
import administration.events.SettingsCreatedEvent
import java.util.UUID
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InitializeSettingsscenarioTest {

  private lateinit var fixture: FixtureConfiguration<SettingsAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(SettingsAggregate::class.java)
  }

  @Test
  fun `Initialize Settingsscenario Test`() {

    val command =
            CreateSettingsCommand(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")
            )

    val expectedEvent =
            SettingsCreatedEvent(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")
            )

    fixture.givenNoPriorActivity()
            .`when`(command)
            .expectSuccessfulHandlerExecution()
            .expectEvents(expectedEvent)
  }
}
