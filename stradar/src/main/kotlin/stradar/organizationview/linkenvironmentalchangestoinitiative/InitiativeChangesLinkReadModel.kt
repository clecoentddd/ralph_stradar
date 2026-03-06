package stradar.organizationview.linkenvironmentalchangestoinitiative

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "initiative_env_links")
class InitiativeChangesLinkReadModel(
        @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
        @Column(nullable = false) val initiativeId: UUID,
        @Column(nullable = false) val envChangeId: UUID,
        @Column(nullable = false) val envChangeName: String,
        @Column(nullable = false) val organizationId: UUID
)

data class GetEnvironmentalLinksByInitiativeQuery(val initiativeId: UUID)
