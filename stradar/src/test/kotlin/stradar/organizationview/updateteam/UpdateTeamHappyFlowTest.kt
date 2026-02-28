package stradar.organizationview.updateteam

import java.util.UUID
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.common.Event
import stradar.common.support.RandomData
import stradar.domain.TeamAggregate
import stradar.events.TeamCreatedEvent
import stradar.events.TeamUpdatedEvent
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand

/**
 * Happy Flow
 *
 * Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661650936247
 */
class UpdateTeamHappyFlowTest {

  private lateinit var fixture: FixtureConfiguration<TeamAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(TeamAggregate::class.java)
  }

  @Test
  fun `Update Team Happy Flow Test`() {

    val teamId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000123")
    val organizationId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000456")

    // GIVEN
    val events = mutableListOf<Event>()

    events.add(
            RandomData.newInstance<TeamCreatedEvent> {
              this.teamId = teamId
              this.context = "Context v1"
              this.level = 1
              this.name = "Name 1"
              this.organizationId = organizationId
              this.purpose = "Purpose 1"
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
              this.teamId = teamId
              this.context = "Context v2"
              this.level = 2
              this.name = "Name 2"
              this.organizationId = organizationId
              this.purpose = "Purpose 2"
            }
    )

    fixture.given(events)
            .`when`(command)
            .expectSuccessfulHandlerExecution()
            .expectEvents(*expectedEvents.toTypedArray())
  }
}
