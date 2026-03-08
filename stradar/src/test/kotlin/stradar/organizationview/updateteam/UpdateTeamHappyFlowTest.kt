package stradar.organizationview.updateteam

import java.util.UUID
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.domain.TeamAggregate
import stradar.events.TeamCreatedEvent
import stradar.events.TeamUpdatedEvent
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand

/** Happy Flow: Updating an existing team within the same organization. */
class UpdateTeamHappyFlowTest {

  private lateinit var fixture: FixtureConfiguration<TeamAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(TeamAggregate::class.java)
  }

  @Test
  fun `Update Team Happy Flow Test`() {
    val teamId = UUID.fromString("00000000-0000-0000-0000-000000000123")
    val organizationId = UUID.fromString("00000000-0000-0000-0000-000000000456")

    // GIVEN: The team was already created in the past
    val initialEvent =
            TeamCreatedEvent(
                    teamId = teamId,
                    organizationId = organizationId,
                    context = "Context v1",
                    level = 1,
                    name = "Name 1",
                    purpose = "Purpose 1",
                    status = "ACTIVE"
            )

    // WHEN: We issue an update command with the correct organizationId in MetaData
    val command =
            UpdateTeamCommand(
                    teamId = teamId,
                    organizationId =
                            organizationId, // Included in command for clarity, but MetaData is the
                    // guard
                    context = "Context v2",
                    level = 2,
                    name = "Name 2",
                    purpose = "Purpose 2"
            )

    // THEN: We expect a TeamUpdatedEvent with the new values and 'ACTIVE' status
    val expectedEvent =
            TeamUpdatedEvent(
                    teamId = teamId,
                    organizationId = organizationId,
                    context = "Context v2",
                    level = 2,
                    name = "Name 2",
                    purpose = "Purpose 2",
                    status = "ACTIVE"
            )

    fixture.given(initialEvent)
            .`when`(command, MetaData.with("organizationId", organizationId))
            .expectSuccessfulHandlerExecution()
            .expectEvents(expectedEvent)
  }
}
