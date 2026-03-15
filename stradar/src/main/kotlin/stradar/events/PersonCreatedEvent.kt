package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661498040166
*/
data class PersonCreatedEvent(
    var organizationId: UUID,
    var organizationName: String,
    var personId: UUID,
    var role: String,
    var username: String,
    var auth0UserId: String? = null
) : Event
