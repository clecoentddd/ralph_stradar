package stradar.organizationview.domain.commands.createdraftstrategy

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreateDraftStrategyCommand(
        @TargetAggregateIdentifier val strategyBuilderId: String,
        val teamId: UUID,
        val organizationId: UUID,
        val strategyId: UUID,
        val strategyName: String,
        val strategyTimeframe: String
)
