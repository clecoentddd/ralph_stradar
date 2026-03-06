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

@Embeddable
data class InitiativeItem(
        var id: UUID? = null,
        var content: String? = null,
        var status: String? = null,
        var step: String? = null
)

@Entity
class InitiativesReadModelEntity {
        @Id @Column(name = "initiativeId") var initiativeId: UUID? = null

        @Column(name = "initiativeName") var initiativeName: String? = null

        @Column(name = "organizationId") var organizationId: UUID? = null

        @Column(name = "strategyId") var strategyId: UUID? = null

        @Column(name = "teamId") var teamId: UUID? = null

        @Column(name = "statut") var statut: String? = "Draft"

        @ElementCollection
        @CollectionTable(
                name = "initiative_items",
                joinColumns = [JoinColumn(name = "initiative_id")]
        )
        var allItems: MutableList<InitiativeItem> = mutableListOf()
}

/** The "Answer" for the coherent list query */
data class InitiativeListResponse(
        val strategyId: UUID,
        val items: List<InitiativesReadModelEntity>
)

/** The "Question" for the coherent list query */
data class InitiativesByStrategyQuery(
        val strategyId: UUID,
        val teamId: UUID,
        val organizationId: UUID
)

/** The "Question" for a single initiative */
data class InitiativesReadModelQuery(val initiativeId: UUID)

/** Wrapper for a single result (matches your QueryHandler return type) */
data class InitiativesReadModel(val data: InitiativesReadModelEntity)
