package stradar.events

import java.util.UUID
import stradar.common.NoArg

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645827622167
*/
@NoArg
data class OrganizationDefinedEvent(
        val organizationId: UUID,
        val personId: UUID,
        val username: String, // The Person context will use this
        val organizationName: String,
        val role: String // The Person context will use this to set permissions
)
