package stradar.organizationview.createdraftstrategy

import java.util.UUID
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.events.StrategyDraftCreatedEvent
import stradar.organizationview.domain.StrategyBuilderAggregate
import stradar.organizationview.domain.commands.createdraftstrategy.CreateDraftStrategyCommand

/** Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661685246929 */
class CreateDraftStrategyOnlyOneDraftStrategyAtATimeTest {

        private lateinit var fixture: FixtureConfiguration<StrategyBuilderAggregate>

        @BeforeEach
        fun setUp() {
                fixture = AggregateTestFixture(StrategyBuilderAggregate::class.java)
        }

        @Test
        fun `Create Draft Strategy Only One Draft Strategy At A Time Test`() {

                val sharedStrategyBuilderId =
                        "18ed5446-4fc6-4dd5-8e98-5b9c5cbf130d-STRATEGY-BUILDER"
                val teamId = UUID.fromString("18ed5446-4fc6-4dd5-8e98-5b9c5cbf130d")
                val orgId = UUID.fromString("474e4828-a953-4240-bb26-368bb332398e")

                // GIVEN: Created via constructor to respect 'val' immutability
                val givenEvent =
                        StrategyDraftCreatedEvent(
                                strategyBuilderId = sharedStrategyBuilderId,
                                strategyId =
                                        UUID.fromString("77777777-7777-7777-7777-777777777777"),
                                organizationId = orgId,
                                teamId = teamId,
                                strategyName = "Initial Plan",
                                strategyTimeframe = "2026"
                        )

                // WHEN: Attempting to create a second draft
                val command =
                        CreateDraftStrategyCommand(
                                strategyBuilderId = sharedStrategyBuilderId,
                                teamId = teamId,
                                organizationId = orgId,
                                strategyId =
                                        UUID.fromString("99999999-9999-9999-9999-999999999999"),
                                strategyName = "A Different Strategy",
                                strategyTimeframe = "2026-Q2"
                        )

                fixture.given(givenEvent)
                        .`when`(command, MetaData.with("organizationId", orgId))
                        .expectException(IllegalStateException::class.java)
                        .expectExceptionMessage("There is already a DRAFT strategy.")
        }
}
