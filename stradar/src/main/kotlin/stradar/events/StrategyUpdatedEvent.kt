package stradar.events

import java.util.UUID
import stradar.common.Event
import stradar.common.StrategyStatus

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764662904816688
*/
data class StrategyUpdatedEvent(
    var strategyBuilderId: String,
    var organizationId: UUID,
    var strategyId: UUID,
    var strategyName: String,
    var strategyStatus: StrategyStatus,
    var strategyTimeframe: String,
    var teamId: UUID
) : Event
