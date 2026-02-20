package administration.events

import administration.common.Event
import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713522
*/
data class InvoiceStateMappingUpdateRequestedEvent(var settingsId: UUID, var connectionId: UUID) :
    Event
