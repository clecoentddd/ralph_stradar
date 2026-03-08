package stradar.organizationview.deleteteam

import java.util.UUID
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.common.Event
import stradar.domain.TeamAggregate
import stradar.events.TeamCreatedEvent
import stradar.events.TeamDeletedEvent
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand

/**
 * Publish Event Team Deleted
 *
 * Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661632345962
 */
class DeleteTeamHappyFlowTest {

        private lateinit var fixture: FixtureConfiguration<TeamAggregate>

        @BeforeEach
        fun setUp() {
                fixture = AggregateTestFixture(TeamAggregate::class.java)
        }

        @Test
        fun `Delete Team Happy Flow Test`() {

                val teamId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000123")
                val organizationId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000456")
                val validReason: String = "Reason 1"

                // GIVEN
                val events = mutableListOf<Event>()
                events.add(
                        TeamCreatedEvent(
                                teamId = teamId,
                                context = "Context 1",
                                level = 3,
                                name = "CTO",
                                organizationId = organizationId,
                                purpose = "Purpose 1"
                        )
                )

                // WHEN
                val command =
                        DeleteTeamCommand(
                                teamId = teamId,
                                organizationId = organizationId,
                                reason = validReason
                        )

                // THEN
                val expectedEvents = mutableListOf<Event>()
                expectedEvents.add(
                        TeamDeletedEvent(
                                teamId = teamId,
                                organizationId = organizationId,
                                status = "DELETED",
                                reason = validReason
                        )
                )

                fixture.given(events)
                        .`when`(command, MetaData.with("organizationId", organizationId))
                        .expectSuccessfulHandlerExecution()
                        .expectEvents(*expectedEvents.toTypedArray())
        }
}
