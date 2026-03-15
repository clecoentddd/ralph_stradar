package stradar.organizationview.updatestrategy

import java.util.UUID
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import stradar.common.CommandException
import stradar.common.Event
import stradar.common.StrategyStatus
import stradar.common.support.RandomData
import stradar.events.StrategyCreatedEvent
import stradar.organizationview.domain.StrategyBuilderAggregate
import stradar.organizationview.domain.commands.createstrategy.CreateStrategyCommand
import stradar.support.metadata.ORGANIZATION_ID_HEADER

/**
 * Rule
 *
 * Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764662910462484
 */
class CanCreateOneAndOnlyOneActiveStrategyTest {

  private lateinit var fixture: FixtureConfiguration<StrategyBuilderAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(StrategyBuilderAggregate::class.java)
  }

  @Test
  fun `Modify Strategy Only One Active Strategy Test`() {

    var strategyBuilderId: String = "12ddd4cc-d8ae-4b70-b74e-de5541eb5671-STRATEGY-BUILDER"

    // GIVEN
    val events = mutableListOf<Event>()

    events.add(
        StrategyCreatedEvent(
            strategyBuilderId = strategyBuilderId,
            organizationId = UUID.fromString("3d2730fc-26f8-4d22-a03e-655f31a0ad6c"),
            strategyId = UUID.fromString("38248734-48b7-4310-ad09-fc833834d5ab"),
            strategyName = "Strategy 1",
            strategyStatus = StrategyStatus.ACTIVE,
            strategyTimeframe = RandomData.newInstance {},
            teamId = UUID.fromString("12ddd4cc-d8ae-4b70-b74e-de5541eb5671")))

    // WHEN
    val command =
        CreateStrategyCommand(
            strategyBuilderId = strategyBuilderId,
            organizationId = UUID.fromString("3d2730fc-26f8-4d22-a03e-655f31a0ad6c"),
            strategyId = UUID.fromString("11111111-48b7-4310-ad09-fc8338111111"),
            strategyName = "Strategy 2",
            strategyStatus = StrategyStatus.ACTIVE,
            strategyTimeframe = RandomData.newInstance {},
            teamId = UUID.fromString("12ddd4cc-d8ae-4b70-b74e-de5541eb5671"))

    // THEN
    val expectedEvents = mutableListOf<Event>()

    fixture
        .given(events)
        .`when`(command, MetaData.with(ORGANIZATION_ID_HEADER, command.organizationId))
        .expectException(CommandException::class.java)
        .expectExceptionMessage("There is already an ACTIVE strategy.")
  }
}
