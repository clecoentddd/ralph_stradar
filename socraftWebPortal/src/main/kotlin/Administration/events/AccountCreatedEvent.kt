package administration.events

import administration.common.Event
import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660029388824
*/
data class AccountCreatedEvent(
        var clientId: UUID,
        var clientEmail: String,
        var companyId: Long,
        var connectionId: UUID
) : Event
