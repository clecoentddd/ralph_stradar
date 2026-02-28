package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645830373813
*/
data class TeamCreatedEvent(
        var teamId: UUID,
        var adminAccountId: UUID,
        var context: String,
        var level: Int,
        var name: String,
        var organizationId: UUID,
        var organizationName: String,
        var purpose: String
) : Event
