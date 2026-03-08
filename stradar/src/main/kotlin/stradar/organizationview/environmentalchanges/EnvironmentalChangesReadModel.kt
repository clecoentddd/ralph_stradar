package stradar.organizationview.environmentalchanges

import jakarta.persistence.*
import java.util.UUID
import stradar.common.*

/** * Query by specific Environmental Change ID */
data class EnvironmentalChangesReadModelQuery(val environmentalChangeId: UUID)

/** * Query by Team ID */
data class EnvironmentalChangesTeamListQuery(val teamId: UUID)

/** The API Response DTO. This "hoists" the shared IDs to the top level. */
data class EnvironmentalChangesReadModel(
        val environmentalChangeId: UUID,
        val teamId: UUID,
        val organizationId: UUID,
        val elements: List<EnvironmentalChangeElementDTO>
)

/** A clean DTO for the elements, containing only element-specific data. */
data class EnvironmentalChangeElementDTO(
        val environmentalChangeId: UUID,
        val title: String,
        val detect: String,
        val assess: String,
        val respond: String,
        val type: ChangeType,
        val category: ChangeCategory,
        val distance: ChangeDistance,
        val impact: ChangeImpact,
        val risk: ChangeRisk
)

/** The Storage Entity. Flat structure: environmentalChangeId is the Primary Key. */
@NoArg
@Entity
@Table(name = "environmental_change_view_read_model")
class EnvironmentalChangesReadModelEntity {

        @Id // Defined as primary key since environmentalChangeId is gone
        @Column(name = "environmental_change_id")
        var environmentalChangeId: UUID? = null

        @Column(name = "team_id") var teamId: UUID? = null

        @Column(name = "organization_id") var organizationId: UUID? = null

        @Column(columnDefinition = "TEXT") var title: String = ""

        @Column(columnDefinition = "TEXT") var detect: String = ""

        @Column(columnDefinition = "TEXT") var assess: String = ""

        @Column(columnDefinition = "TEXT") var respond: String = ""

        @Enumerated(EnumType.STRING) var type: ChangeType = ChangeType.THREAT

        @Enumerated(EnumType.STRING) var category: ChangeCategory = ChangeCategory.CAPABILITIES

        @Enumerated(EnumType.STRING) var distance: ChangeDistance = ChangeDistance.DETECTED

        @Enumerated(EnumType.STRING) var impact: ChangeImpact = ChangeImpact.LOW

        @Enumerated(EnumType.STRING) var risk: ChangeRisk = ChangeRisk.LOW
}
