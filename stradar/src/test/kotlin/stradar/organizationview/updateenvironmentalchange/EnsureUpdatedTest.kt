package stradar.organizationview.updateenvironmentalchange

import java.util.UUID
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.common.*
import stradar.common.support.RandomData
import stradar.domain.EnvironmentalChangeAggregate
import stradar.events.EnvironmentalChangeDetectedEvent
import stradar.events.EnvironmentalChangeUpdatedEvent
import stradar.organizationview.domain.commands.updateenvironmentalchange.UpdateEnvironmentalChangeCommand
import stradar.support.metadata.ORGANIZATION_ID_HEADER

class EnsureUpdatedTest {

        private lateinit var fixture: FixtureConfiguration<EnvironmentalChangeAggregate>

        @BeforeEach
        fun setUp() {
                // Pointing to the new standalone aggregate
                fixture = AggregateTestFixture(EnvironmentalChangeAggregate::class.java)
        }

        @Test
        fun `Update Environmental Change Ensure Updated Test`() {

                val organizationId = UUID.randomUUID()
                val teamId = UUID.randomUUID()
                val environmentalChangeId = UUID.randomUUID()

                // GIVEN: The change already exists in the system
                val events =
                        listOf(
                                RandomData.newInstance<EnvironmentalChangeDetectedEvent> {
                                        this.environmentalChangeId = environmentalChangeId
                                        this.teamId = teamId
                                        this.organizationId = organizationId
                                        this.assess = "assess v1"
                                        this.category = ChangeCategory.CAPABILITIES
                                        this.detect = ""
                                        this.distance = ChangeDistance.DETECTED
                                        this.impact = ChangeImpact.HIGH
                                        this.respond = ""
                                        this.risk = ChangeRisk.LOW
                                        this.title = "Initial Title"
                                        this.type = ChangeType.OPPORTUNITY
                                }
                        )

                // WHEN: We update the aggregate
                val command =
                        UpdateEnvironmentalChangeCommand(
                                environmentalChangeId = environmentalChangeId,
                                teamId = teamId,
                                organizationId = organizationId,
                                assess = "assess v2",
                                category = ChangeCategory.BUSINESS,
                                detect = "Detection update",
                                distance = ChangeDistance.DETECTED,
                                impact = ChangeImpact.HIGH,
                                respond = "New Response",
                                risk = ChangeRisk.LOW,
                                title = "Updated Title",
                                type = ChangeType.OPPORTUNITY
                        )

                // THEN: We expect the Updated event with the new data
                val expectedEvent =
                        EnvironmentalChangeUpdatedEvent(
                                environmentalChangeId = environmentalChangeId,
                                teamId = teamId,
                                organizationId = organizationId,
                                assess = command.assess,
                                category = command.category,
                                detect = command.detect,
                                distance = command.distance,
                                impact = command.impact,
                                respond = command.respond,
                                risk = command.risk,
                                title = command.title,
                                type = command.type
                        )

                fixture.given(events)
                        .`when`(command, MetaData.with(ORGANIZATION_ID_HEADER, organizationId))
                        .expectSuccessfulHandlerExecution()
                        .expectEvents(expectedEvent)
        }
}
