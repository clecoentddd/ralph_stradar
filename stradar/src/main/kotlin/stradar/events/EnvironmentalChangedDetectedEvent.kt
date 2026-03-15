package stradar.events

import java.util.UUID
import stradar.common.*

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661032650856
*/
data class EnvironmentalChangeDetectedEvent(
    var environmentalChangeId: UUID,
    var teamId: UUID,
    var organizationId: UUID,
    var assess: String,
    var category: ChangeCategory,
    var detect: String,
    var distance: ChangeDistance,
    var impact: ChangeImpact,
    var respond: String,
    var risk: ChangeRisk,
    var title: String,
    var type: ChangeType
) : Event
