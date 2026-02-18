package administration.events

import administration.common.Event
import administration.common.ListOfInvoicesItem
import java.util.UUID
import kotlin.collections.List

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660086962397
*/
data class InvoicesFetchedEvent(
        var companyId: Long,
        var clientId: UUID,
        var invoiceList: List<ListOfInvoicesItem>
) : Event
