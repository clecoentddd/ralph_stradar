package stradar.organizationview.initiativelist

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645855652122
*/

// ─── 1. DATABASE ENTITIES (The Storage) ──────────────────────────────────────

@Embeddable
data class InitiativeItem(
        var id: UUID? = null,
        var content: String? = null,
        var status: String? = null,
        var step: String? = null // e.g., "DIAGNOSTIC", "COHERENTACTION"
)

@Entity
class InitiativesReadModelEntity {
        @Id @Column(name = "initiativeId") var initiativeId: UUID? = null

        @Column(name = "initiativeName") var initiativeName: String? = null

        @Column(name = "organizationId") var organizationId: UUID? = null

        @Column(name = "strategyId") var strategyId: UUID? = null

        @Column(name = "teamId") var teamId: UUID? = null

        @Column(name = "statut") var statut: String? = "Draft" // Values: Draft, Active, Deleted

        @ElementCollection
        @CollectionTable(
                name = "initiative_items",
                joinColumns = [JoinColumn(name = "initiative_id")]
        )
        var allItems: MutableList<InitiativeItem> = mutableListOf()
}

// ─── 2. THE QUESTIONS (Queries) ──────────────────────────────────────────────

/**
 * * Request all non-deleted initiatives for a specific organization. Used to populate the
 * high-level organization dashboard.
 */
data class AllInitiativesForOrganizationQuery(val organizationId: UUID)

/** Request initiatives filtered by strategy and team context */
data class InitiativesByStrategyQuery(
        val strategyId: UUID,
        val teamId: UUID,
        val organizationId: UUID
)

/** Request details for a single specific initiative (e.g., clicking a radar dot) */
data class InitiativesReadModelQuery(val initiativeId: UUID)

// ─── 3. THE ANSWERS (Responses / DTOs) ───────────────────────────────────────

/**
 * * The structured response for the Organization View. Groups data so the UI can easily render Team
 * -> Level -> Items.
 */
data class OrganizationInitiativeListResponse(
        val organizationId: UUID,
        val items: List<InitiativesReadModelEntity>
)

data class TeamInitiativesDTO(
        val teamId: UUID,
        val teamName: String,
        // Grouped by level (StepKey: DIAGNOSTIC, etc.)
        val levels: Map<String, List<InitiativesReadModelEntity>>
)

/** Simple list wrapper for strategy-specific queries */
data class InitiativeListResponse(
        val strategyId: UUID,
        val items: List<InitiativesReadModelEntity>
)

/** Wrapper for single entity results (QueryHandler return type) */
data class InitiativesReadModel(val data: InitiativesReadModelEntity)
