package stradar.organizationview.domain.commands.createdraftstrategy

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.StrategyDetails

data class CreateDraftStrategyCommand(
        @TargetAggregateIdentifier var strategyBuilderId: String,
        var teamId: UUID,
        var organizationId: UUID,
        var strategy: StrategyDetails // Renamed for clarity
)
