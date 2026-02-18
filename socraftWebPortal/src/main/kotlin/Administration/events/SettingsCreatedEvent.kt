package administration.events

import administration.common.Event
import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659756306799
*/
data class SettingsCreatedEvent(var settingsId: UUID, var connectionId: UUID) : Event
