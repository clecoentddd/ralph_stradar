package stradar.organizationview.domain.commands.createstrategy

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.StrategyStatus

data class CreateStrategyCommand(
        @TargetAggregateIdentifier val strategyBuilderId: String,
        val teamId: UUID,
        val organizationId: UUID,
        val strategyId: UUID,
        val strategyName: String,
        val strategyTimeframe: String,
        val strategyStatus: StrategyStatus
)
