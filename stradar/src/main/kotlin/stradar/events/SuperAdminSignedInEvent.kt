package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660815509287
*/
data class SuperAdminSignedInEvent(var adminAccountId: UUID, var username: String) : Event
