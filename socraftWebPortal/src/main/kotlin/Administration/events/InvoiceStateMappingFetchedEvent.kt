package administration.events

import administration.common.Event
import administration.common.InvoiceState
import java.util.UUID
import kotlin.collections.List

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713525
*/
data class InvoiceStateMappingFetchedEvent(
        var settingsId: UUID,
        var connectionId: UUID,
        var listOfInvoiceStates: List<InvoiceState>,
) : Event
