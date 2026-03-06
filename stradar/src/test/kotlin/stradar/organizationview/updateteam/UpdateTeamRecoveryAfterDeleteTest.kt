package stradar.organizationview.updateteam

import java.util.UUID
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.common.Event
import stradar.common.support.RandomData
import stradar.domain.TeamAggregate
import stradar.events.TeamCreatedEvent
import stradar.events.TeamDeletedEvent
import stradar.events.TeamUpdatedEvent
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand

/**
 * User can update a team after deleting it (recovery mechanism)
 *
 * Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661658628233
 */
class UpdateTeamRecoveryAfterDeleteTest {

  private lateinit var fixture: FixtureConfiguration<TeamAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(TeamAggregate::class.java)
  }

  @Test
  fun `Update Team Recovery After Delete Test`() {

    val teamId: UUID = RandomData.newInstance<UUID> {}
    val organizationId: UUID = RandomData.newInstance<UUID> {}

    // GIVEN
    val events = mutableListOf<Event>()

    events.add(
            RandomData.newInstance<TeamCreatedEvent> {
              this.teamId = teamId
              this.context = RandomData.newInstance {}
              this.level = RandomData.newInstance {}
              this.name = RandomData.newInstance {}
              this.organizationId = organizationId
              this.purpose = RandomData.newInstance {}
            }
    )

    events.add(
            RandomData.newInstance<TeamDeletedEvent> {
              this.teamId = teamId
              this.organizationId = organizationId
            }
    )

    // WHEN
    val command =
            UpdateTeamCommand(
                    teamId = teamId,
                    context = "Context v2",
                    level = 2,
                    name = "Name 2",
                    organizationId = organizationId,
                    purpose = "Purpose 2"
            )

    // THEN
    val expectedEvents = mutableListOf<Event>()

    expectedEvents.add(
            RandomData.newInstance<TeamUpdatedEvent> {
              this.teamId = command.teamId
              this.context = command.context
              this.level = command.level
              this.name = command.name
              this.organizationId = command.organizationId
              this.purpose = command.purpose
            }
    )

    fixture.given(events)
            .`when`(command, MetaData.with("organizationId", organizationId))
            .expectSuccessfulHandlerExecution()
            .expectEvents(*expectedEvents.toTypedArray())
  }
}
