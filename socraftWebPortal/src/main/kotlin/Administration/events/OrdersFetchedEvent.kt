package administration.events

import administration.common.Event
import administration.common.OrderDetails
import java.util.UUID
import kotlin.collections.List

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660085256078
*/
data class OrdersFetchedEvent(
        var companyId: Long,
        var clientId: UUID,
        var orderList: List<OrderDetails>
) : Event
