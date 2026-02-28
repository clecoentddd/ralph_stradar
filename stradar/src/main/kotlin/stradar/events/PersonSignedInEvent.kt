package stradar.events

import java.util.UUID

data class PersonSignedInEvent(
        val personId: UUID, // The Identity (Standardized)
        val organizationId: UUID, // The Context
        val role: String // The Power (Standardized)
)
