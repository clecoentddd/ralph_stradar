package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631102656
*/
data class TeamDeletedEvent(
        val teamId: UUID,
        val organizationId: UUID,
        val status: String,
        val reason: String
) : Event
