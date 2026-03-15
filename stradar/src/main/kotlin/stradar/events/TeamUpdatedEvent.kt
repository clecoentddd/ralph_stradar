package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631101975
*/
data class TeamUpdatedEvent(
    var teamId: UUID,
    var context: String,
    var level: Int,
    var name: String,
    var organizationId: UUID,
    var purpose: String,
    val status: String? = null
) : Event
