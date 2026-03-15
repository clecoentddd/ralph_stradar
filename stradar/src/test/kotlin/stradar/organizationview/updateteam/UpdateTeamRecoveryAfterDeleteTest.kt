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
import stradar.support.metadata.ORGANIZATION_ID_HEADER

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
          this.context = "Context 1"
          this.level = 1
          this.name = "Name 1"
          this.organizationId = organizationId
          this.purpose = "Purpose 1"
        })

    events.add(
        TeamDeletedEvent(
            teamId = teamId,
            organizationId = organizationId,
            status = "DELETED",
            reason = "A very good reason"))

    // WHEN
    val command =
        UpdateTeamCommand(
            teamId = teamId,
            context = "Context v2",
            level = 2,
            name = "Name 2",
            organizationId = organizationId,
            purpose = "Purpose 2")

    // THEN
    val expectedEvent =
        TeamUpdatedEvent(
            teamId = teamId,
            organizationId = organizationId,
            context = "Context v2",
            level = 2,
            name = "Name 2",
            purpose = "Purpose 2",
            status = "ACTIVE")

    fixture
        .given(events)
        .`when`(command, MetaData.with(ORGANIZATION_ID_HEADER, organizationId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(expectedEvent)
  }
}
