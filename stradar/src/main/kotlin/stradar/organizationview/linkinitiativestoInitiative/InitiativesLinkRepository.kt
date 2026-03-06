package stradar.organizationview.linkinitiativestoinitiative

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

interface InitiativesLinkRepository : JpaRepository<InitiativesLinkReadModel, UUID> {

    fun findAllByInitiativeIdAndOrganizationId(
            initiativeId: UUID,
            organizationId: UUID
    ): List<InitiativesLinkReadModel>

    @Transactional
    @Modifying
    fun deleteByInitiativeIdAndOrganizationId(initiativeId: UUID, organizationId: UUID)

    /**
     * * Refactored: This handles the "Broken Link" cleanup. If Initiative 'B' is deleted, we must
     * remove all links where 'B' was the target.
     */
    @Transactional
    @Modifying
    fun deleteByLinkedInitiativeIdAndOrganizationId(linkedInitiativeId: UUID, organizationId: UUID)
}
