package stradar.common

import jakarta.persistence.*
import java.util.UUID

@NoArg
@Embeddable
data class EnvironmentalChange(
        @Column(name = "environmental_change_id") val environmentalChangeId: UUID,
        val title: String,
        val detect: String,
        val assess: String,
        val respond: String,
        @Enumerated(EnumType.STRING) val type: ChangeType,
        @Enumerated(EnumType.STRING) val category: ChangeCategory,
        @Enumerated(EnumType.STRING) val distance: ChangeDistance,
        @Enumerated(EnumType.STRING) val impact: ChangeImpact,
        @Enumerated(EnumType.STRING) val risk: ChangeRisk
)
