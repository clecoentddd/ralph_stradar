package Administration.adminconnection

import Administration.common.Event
import Administration.common.support.RandomData
import Administration.domain.AccountAggregate
import Administration.domain.commands.adminconnection.ToConnectCommand
import Administration.events.AdminConnectedEvent
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

  private lateinit var fixture: FixtureConfiguration<AccountAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(AccountAggregate::class.java)
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
                    email = "test@test.com"
            )

    // THEN
    val expectedEvents = mutableListOf<Event>()

    expectedEvents.add(
            RandomData.newInstance<AdminConnectedEvent> {
              this.connectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")
              this.email = "test@test.com"
            }
    )

    fixture.given(events)
            .`when`(command)
            .expectSuccessfulHandlerExecution()
            .expectEvents(*expectedEvents.toTypedArray())
  }
}
