package administration.events

import administration.common.Event
import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734675973
*/
data class AdminConnectedEvent(var connectionId: UUID, var email: String) : Event
