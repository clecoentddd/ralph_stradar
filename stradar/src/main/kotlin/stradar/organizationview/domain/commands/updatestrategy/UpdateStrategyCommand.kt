package stradar.organizationview.domain.commands.updatestrategy

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command
import stradar.common.StrategyStatus

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764662905023711
*/
data class UpdateStrategyCommand(
        @TargetAggregateIdentifier var strategyBuilderId: String,
        var organizationId: UUID,
        var strategyId: UUID,
        var strategyName: String,
        var strategyStatus: StrategyStatus,
        var strategyTimeframe: String,
        var teamId: UUID
) : Command
