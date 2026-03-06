package stradar.organizationview.linkinitiativestoinitiative

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "initiative_to_initiative_links") // Updated table name
class InitiativesLinkReadModel(
        @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
        @Column(nullable = false) val initiativeId: UUID, // The "Source" initiative
        @Column(nullable = false)
        val linkedInitiativeId: UUID, // The "Target" initiative (formerly envChangeId)
        @Column(nullable = false)
        val linkedInitiativeName: String, // Denormalized name for faster display
        @Column(nullable = false) val organizationId: UUID
)

/** * Updated Query name to reflect initiative-to-initiative linking */
data class GetLinkedInitiativesQuery(val initiativeId: UUID, val organizationId: UUID)
