package stradar.organizationview.detectenvironmentalchange

import java.util.UUID
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.common.*
import stradar.domain.EnvironmentalChangeAggregate
import stradar.events.EnvironmentalChangeDetectedEvent
import stradar.organizationview.domain.commands.detectenvironmentalchange.DetectEnvironmentalChangeCommand

/** Environmental Change is detected (Aggregate Creation) */
class DetectEnvironmentalChangeHappyFlowTest {

        private lateinit var fixture: FixtureConfiguration<EnvironmentalChangeAggregate>

        @BeforeEach
        fun setUp() {
                fixture = AggregateTestFixture(EnvironmentalChangeAggregate::class.java)
        }

        @Test
        fun `Detect Environmental Change Happy Flow Test`() {

                val orgId = UUID.randomUUID()
                val teamId = UUID.randomUUID()
                val environmentalChangeId =
                        UUID.randomUUID() // The primary ID for the new aggregate

                // GIVEN: Nothing (Aggregate is being created)

                // WHEN: We send the command to detect a change
                val command =
                        DetectEnvironmentalChangeCommand(
                                environmentalChangeId = environmentalChangeId,
                                teamId = teamId,
                                organizationId = orgId,
                                assess = "Assess v1",
                                category = ChangeCategory.CAPABILITIES,
                                detect = "Initial Detection",
                                distance = ChangeDistance.DETECTED,
                                impact = ChangeImpact.LOW,
                                respond = "respond v1",
                                risk = ChangeRisk.LOW,
                                title = "Tech Shift v1",
                                type = ChangeType.OPPORTUNITY
                        )

                // THEN: We expect the Detected event with the matching environmentalChangeId
                val expectedEvent =
                        EnvironmentalChangeDetectedEvent(
                                environmentalChangeId =
                                        environmentalChangeId, // Must match the command!
                                teamId = teamId,
                                organizationId = orgId,
                                assess = "Assess v1",
                                category = ChangeCategory.CAPABILITIES,
                                detect = "Initial Detection",
                                distance = ChangeDistance.DETECTED,
                                impact = ChangeImpact.LOW,
                                respond = "respond v1",
                                risk = ChangeRisk.LOW,
                                title = "Tech Shift v1",
                                type = ChangeType.OPPORTUNITY
                        )

                fixture.givenNoPriorActivity()
                        .`when`(command)
                        .expectSuccessfulHandlerExecution()
                        .expectEvents(expectedEvent)
        }
}
