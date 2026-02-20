package administration.adminconnection

import administration.admin.domain.commands.adminconnection.ToConnectCommand
import administration.common.Event
import administration.common.support.RandomData
import administration.domain.AdminAccountAggregate
import administration.events.AdminConnectedEvent
import java.util.UUID
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Ensure non null values
 *
 * Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659756708369
 */
class AdminConnectionEnsureNonNullValuesTest {

  private lateinit var fixture: FixtureConfiguration<AdminAccountAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(AdminAccountAggregate::class.java)
  }

  @Test
  fun `Admin Connection Ensure Non Null Values Test`() {

    var connectionId: UUID = RandomData.newInstance<UUID> {}

    // GIVEN
    val events = mutableListOf<Event>()

    // WHEN
    val command =
            ToConnectCommand(
                    connectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8"),
                    email = "test@socraft.ch"
            )

    // THEN
    val expectedEvents = mutableListOf<Event>()

    expectedEvents.add(
            RandomData.newInstance<AdminConnectedEvent> {
              this.connectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")
              this.email = "test@socraft.ch"
            }
    )

    fixture.given(events)
            .`when`(command)
            .expectSuccessfulHandlerExecution()
            .expectEvents(*expectedEvents.toTypedArray())
  }
}
