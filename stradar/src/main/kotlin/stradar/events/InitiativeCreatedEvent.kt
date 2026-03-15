package stradar.events

import java.util.UUID
import stradar.common.Event

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645850767799
*/
data class InitiativeCreatedEvent(
    var initiativeId: UUID,
    var initiativeName: String,
    var organizationId: UUID,
    var strategyId: UUID,
    var teamId: UUID,
    // --- Flattened Detail Lists ---
    // Initialized as empty so Projections can create empty rows/collections
    val diagnostic: List<String> = emptyList(),
    val overallPlan: List<String> = emptyList(),
    val coherentActions: List<String> = emptyList(),
    val proximateObjectives: List<String> = emptyList()
) : Event
