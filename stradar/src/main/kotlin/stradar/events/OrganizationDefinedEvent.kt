package stradar.events

import java.util.UUID
import stradar.common.NoArg

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645827622167
*/
@NoArg
data class OrganizationDefinedEvent(
        val organizationId: UUID,
        val organizationUserEmail: String,
        val organizationUserId: UUID,
        val organizationName: String,
        val role: String
)
