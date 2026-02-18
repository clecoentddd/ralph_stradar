package administration.events

import administration.common.Event
import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822172
*/
data class CompanyListUpdateRequestedEvent(var settingsId: UUID, var connectionId: UUID) : Event
