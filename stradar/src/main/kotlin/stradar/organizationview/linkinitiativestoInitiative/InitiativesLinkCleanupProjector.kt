package stradar.organizationview.linkinitiativestoinitiative

import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.events.InitiativeChangedEvent

@Component
class InitiativesLinkCleanupProjector(private val repository: InitiativesLinkRepository) {

    @EventHandler
    fun on(event: InitiativeChangedEvent, metaData: MetaData) {
        if (event.status == "DELETED") {
            val secureOrgId = metaData.resolveOrganizationId()

            // Clean up where this initiative was the "Source"
            repository.deleteByInitiativeIdAndOrganizationId(event.initiativeId, secureOrgId)

            // Clean up where this initiative was the "Target" of other links
            repository.deleteByLinkedInitiativeIdAndOrganizationId(event.initiativeId, secureOrgId)
        }
    }
}
