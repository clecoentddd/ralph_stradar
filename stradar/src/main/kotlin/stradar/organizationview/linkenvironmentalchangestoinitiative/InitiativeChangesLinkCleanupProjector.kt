package stradar.organizationview.linkenvironmentalchangestoinitiative

import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.events.EnvironmentalChangeDeletedEvent

@Component
class InitiativeChangesLinkCleanupProjector(
    private val repository: InitiativeChangesLinkRepository
) {

  @EventHandler
  fun on(event: EnvironmentalChangeDeletedEvent, metaData: MetaData) {
    val organizationId = metaData.resolveOrganizationId()

    // Securely scoped cleanup
    repository.deleteByEnvChangeIdAndOrganizationId(event.environmentalChangeId, organizationId)
  }
}
