package administration.admin.initializesettings

import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
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

class InitializeSettingsScenarioTest {

        private lateinit var fixture: FixtureConfiguration<SettingsAggregate>

        @BeforeEach
        fun setUp() {
                fixture = AggregateTestFixture(SettingsAggregate::class.java)
        }

        @Test
        fun `Initialize Settingsscenario Test`() {

                val connectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")

                val command =
                        CreateSettingsCommand(
                                settingsId = SettingsConstants.SETTINGS_ID,
                                connectionId = connectionId
                        )

                // UPDATED: Added ADMIN_COMPANY_ID to match the "SOCRAFT_ADMIN_BACKEND" pattern
                val commandMessage =
                        GenericCommandMessage.asCommandMessage<CreateSettingsCommand>(command)
                                .withMetaData(
                                        MetaData.with(
                                                        AdminSecurityHeaders.SESSION_ID,
                                                        "test-session-abc"
                                                )
                                                .and(
                                                        AdminSecurityHeaders.ADMIN_COMPANY_ID,
                                                        "SOCRAFT_ADMIN_BACKEND"
                                                )
                                )

                val expectedEvent =
                        SettingsCreatedEvent(
                                settingsId = SettingsConstants.SETTINGS_ID,
                                connectionId = connectionId
                        )

                fixture.givenNoPriorActivity()
                        .`when`(commandMessage)
                        .expectSuccessfulHandlerExecution()
                        .expectEvents(expectedEvent)
        }
}
