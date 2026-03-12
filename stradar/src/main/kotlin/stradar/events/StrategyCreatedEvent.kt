package stradar.events

import java.util.UUID
import stradar.common.Event
import stradar.common.StrategyStatus

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661671865425
*/
data class StrategyCreatedEvent(
        var strategyBuilderId: String,
        var strategyId: UUID, // The specific instance ID
        var organizationId: UUID,
        var teamId: UUID,
        var strategyName: String, // Flat
        var strategyTimeframe: String, // Flat
        var strategyStatus: StrategyStatus
) : Event
