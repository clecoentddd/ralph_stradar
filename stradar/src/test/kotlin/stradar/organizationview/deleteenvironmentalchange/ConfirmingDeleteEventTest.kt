package stradar.organizationview.deleteenvironmentalchange

import java.util.UUID
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.common.*
import stradar.common.support.RandomData
import stradar.domain.EnvironmentalChangeAggregate
import stradar.events.EnvironmentalChangeDeletedEvent
import stradar.events.EnvironmentalChangeDetectedEvent
import stradar.organizationview.domain.commands.deleteenvironmentalchange.DeleteEnvironmentalChangeCommand

class DeleteEnvironmentalChangeTest {

        private lateinit var fixture: FixtureConfiguration<EnvironmentalChangeAggregate>

        @BeforeEach
        fun setUp() {
                // Now pointing to the new Aggregate
                fixture = AggregateTestFixture(EnvironmentalChangeAggregate::class.java)
        }

        @Test
        fun `Delete Environmental Change Confirming Delete Event Test`() {

                val organizationId = UUID.randomUUID()
                val teamId = UUID.randomUUID()
                val environmentalChangeId = UUID.randomUUID()

                // 1. GIVEN: The change was already detected
                val givenEvent =
                        RandomData.newInstance<EnvironmentalChangeDetectedEvent> {
                                this.environmentalChangeId = environmentalChangeId
                                this.teamId = teamId
                                this.organizationId = organizationId
                                this.category = ChangeCategory.CAPABILITIES
                                this.distance = ChangeDistance.DETECTED
                                this.impact = ChangeImpact.HIGH
                                this.risk = ChangeRisk.LOW
                                this.type = ChangeType.OPPORTUNITY
                                this.title = "Incoming Tech Shift"
                        }

                // 2. WHEN: We delete the aggregate itself
                val command =
                        DeleteEnvironmentalChangeCommand(
                                environmentalChangeId = environmentalChangeId,
                                teamId = teamId,
                                organizationId = organizationId
                        )

                // 3. EXPECT: The deleted event to be emitted
                val expectedEvent =
                        EnvironmentalChangeDeletedEvent(
                                environmentalChangeId = environmentalChangeId,
                                organizationId = organizationId,
                                teamId = teamId
                        )

                fixture.given(givenEvent)
                        .`when`(command)
                        .expectSuccessfulHandlerExecution()
                        .expectEvents(expectedEvent)
        }
}
