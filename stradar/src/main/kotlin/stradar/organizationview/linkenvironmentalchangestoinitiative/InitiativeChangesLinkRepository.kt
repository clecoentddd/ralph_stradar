package stradar.organizationview.linkenvironmentalchangestoinitiative

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

interface InitiativeChangesLinkRepository : JpaRepository<InitiativeChangesLinkReadModel, UUID> {

    fun findAllByInitiativeIdAndOrganizationId(
            initiativeId: UUID,
            organizationId: UUID
    ): List<InitiativeChangesLinkReadModel>

    @Transactional
    @Modifying
    fun deleteByInitiativeIdAndOrganizationId(initiativeId: UUID, organizationId: UUID)

    @Transactional
    @Modifying
    fun deleteByEnvChangeIdAndOrganizationId(envChangeId: UUID, organizationId: UUID)
}
