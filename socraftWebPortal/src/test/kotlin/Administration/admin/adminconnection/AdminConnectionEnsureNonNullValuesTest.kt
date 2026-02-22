package administration.admin.adminconnection

import administration.admin.domain.commands.adminconnection.ToConnectCommand
import administration.common.Event
import administration.common.support.RandomData
import administration.domain.AdminAccountAggregate
import administration.events.AdminConnectedEvent
import administration.support.metadata.AppSecurityHeaders // Added
import java.util.UUID
import org.axonframework.commandhandling.GenericCommandMessage // Added
import org.axonframework.messaging.MetaData // Added
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdminConnectionEnsureNonNullValuesTest {

  private lateinit var fixture: FixtureConfiguration<AdminAccountAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(AdminAccountAggregate::class.java)
  }

  @Test
  fun `Admin Connection Ensure Non Null Values Test`() {

    val connectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")

    // GIVEN
    val events = mutableListOf<Event>()

    // WHEN
    val command = ToConnectCommand(connectionId = connectionId, email = "test@socraft.ch")

    // Create metadata to satisfy the Interceptor
    val metaData =
            MetaData.with(AppSecurityHeaders.SESSION_ID_HEADER, "test-session-123")
                    .and(AppSecurityHeaders.COMPANY_ID_HEADER, "MAIN_COMPANY_789")

    // Wrap the command in a message that includes the metadata
    val commandMessage =
            GenericCommandMessage.asCommandMessage<ToConnectCommand>(command).withMetaData(metaData)

    // THEN
    val expectedEvent =
            RandomData.newInstance<AdminConnectedEvent> {
              this.connectionId = connectionId
              this.email = "test@socraft.ch"
            }

    fixture.given(events)
            .`when`(commandMessage) // Pass the message instead of the raw command
            .expectSuccessfulHandlerExecution()
            .expectEvents(expectedEvent)
  }
}
