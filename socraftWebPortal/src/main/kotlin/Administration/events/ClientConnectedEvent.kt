package administration.events

import administration.common.Event
import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660036569902
*/
data class ClientConnectedEvent(var clientId: UUID, var clientEmail: String, var companyId: Long) :
        Event
