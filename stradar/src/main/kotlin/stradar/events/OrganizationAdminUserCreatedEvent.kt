package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764663408532420
*/
data class OrganizationAdminUserCreatedEvent(
        var organizationId: UUID,
        var organizationName: String,
        var role: String,
        var organizationUserId: UUID,
        var organizationUserEmail: String,
        var auth0UserId: String
) : Event
