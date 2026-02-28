package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661033170251
*/
data class EnvironmentalChangeDeletedEvent(
        var environmentalChangeId: UUID,
        var teamId: UUID,
        var organizationId: UUID
) : Event
