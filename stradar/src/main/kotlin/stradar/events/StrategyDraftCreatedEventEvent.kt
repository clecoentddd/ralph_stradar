package stradar.events

import stradar.common.Event

import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661671789872
*/
data class StrategyDraftCreatedEventEvent(
    var strategyBuilderId:String,
	var organizationId:UUID,
	var strategyId:UUID,
	var strategyName:String,
	var strategyTimeframe:String,
	var teamId:UUID
) : Event
