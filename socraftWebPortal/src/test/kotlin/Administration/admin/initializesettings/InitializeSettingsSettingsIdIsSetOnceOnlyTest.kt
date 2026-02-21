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

class InitializeSettingsSettingsIdIsSetOnceOnlyTest {

        private lateinit var fixture: FixtureConfiguration<SettingsAggregate>

        private val testConnectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")

        // Coherent Metadata: Matches the "SOCRAFT_ADMIN_BACKEND" pattern from your Resource
        private val testMetaData =
                MetaData.with(AdminSecurityHeaders.SESSION_ID, "test-session-123")
                        .and(AdminSecurityHeaders.ADMIN_COMPANY_ID, "SOCRAFT_ADMIN_BACKEND")

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
                        // This verifies the EventSourcingHandler worked and the state is held
                        .expectState { state -> assert(state.connectionId == testConnectionId) }
        }

        @Test
        fun `should ignore duplicate creation by returning early`() {
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

                // GIVEN: The aggregate already exists (lifecycle is initialized)
                fixture.given(existingEvent)
                        .`when`(commandMessage)
                        // THEN: It should not throw an error, but it should NOT apply new events
                        .expectSuccessfulHandlerExecution()
                        .expectNoEvents()
        }

        @Test
        fun `should throw exception when trying to overwrite with different ID`() {
                val differentId = UUID.randomUUID()
                val command =
                        CreateSettingsCommand(
                                settingsId = differentId,
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

                fixture.given(existingEvent)
                        .`when`(commandMessage)
                        .expectException(CommandException::class.java)
                        .expectExceptionMessage(
                                "Cannot create SettingsAggregate with a different settingsId"
                        )
        }
}
