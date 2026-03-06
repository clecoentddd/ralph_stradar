package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661874616036
*/
data class InitiativeChangedEvent(
        var initiativeId: UUID,
        var initiativeName: String,
        var organizationId: UUID,
        var status: String
) : Event
