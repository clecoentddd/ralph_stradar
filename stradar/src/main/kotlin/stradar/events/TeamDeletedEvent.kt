package stradar.events

import stradar.common.Event

import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631102656
*/
data class TeamDeletedEvent(
    var teamId:UUID,
	var organizationId:UUID
) : Event
