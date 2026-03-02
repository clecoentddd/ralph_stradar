package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661764455538
*/
data class InitiativeItemChangedEvent(
        var initiativeId: UUID,
        var step: String,
        var itemId: UUID,
        var content: String,
        var status: String
) : Event
