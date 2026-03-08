package stradar.organizationview.createteam

import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import stradar.domain.TeamAggregate
import stradar.events.TeamCreatedEvent
import stradar.organizationview.domain.commands.createteam.CreateTeamCommand
import stradar.organizationview.teamlist.TeamNameAlreadyExistsQuery

class TeamCreatedTest {

        private lateinit var fixture: FixtureConfiguration<TeamAggregate>
        private lateinit var queryGateway: QueryGateway

        @BeforeEach
        fun setUp() {
                fixture = AggregateTestFixture(TeamAggregate::class.java)
                queryGateway = mock(QueryGateway::class.java)
                fixture.registerInjectableResource(queryGateway)
        }

        @Test
        fun `Team Created Test`() {
                val orgId = UUID.randomUUID()
                val adminId = UUID.randomUUID()
                val teamId = UUID.randomUUID()

                // Stub QueryGateway to return false (not a duplicate) for any uniqueness check
                `when`(
                                queryGateway.query(
                                        any(TeamNameAlreadyExistsQuery::class.java),
                                        any(Class::class.java)
                                )
                        )
                        .thenReturn(CompletableFuture.completedFuture(false))

                // WHEN
                val command =
                        CreateTeamCommand(
                                teamId = teamId,
                                organizationId = orgId,
                                context = "IT",
                                level = 1,
                                name = "CTO",
                                purpose = "To do good"
                        )

                // THEN
                val expectedEvent =
                        TeamCreatedEvent(
                                teamId = teamId,
                                organizationId = orgId,
                                context = "IT",
                                level = 1,
                                name = "CTO",
                                purpose = "To do good",
                                status = "ACTIVE" // <--- MUST BE HARDCODED TO "ACTIVE"
                        )

                fixture.givenNoPriorActivity()
                        .`when`(command, MetaData.with("organizationId", orgId))
                        .expectSuccessfulHandlerExecution()
                        .expectEvents(expectedEvent)
        }
}
