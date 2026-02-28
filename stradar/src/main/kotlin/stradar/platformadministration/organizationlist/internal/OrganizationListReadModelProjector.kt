package stradar.platformadministration.organizationlist.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import stradar.events.OrganizationDefinedEvent
import stradar.platformadministration.ProcessingGroups
import stradar.platformadministration.organizationlist.OrganizationListReadModelEntity

interface OrganizationListReadModelRepository :
        JpaRepository<OrganizationListReadModelEntity, UUID> {
  fun existsByUsername(username: String): Boolean
}

@Component
@ProcessingGroup(ProcessingGroups.PLATFORM_ADMINISTRATION)
class OrganizationListReadModelProjector(
        private val repository: OrganizationListReadModelRepository
) {

  private val logger = KotlinLogging.logger {}

  @EventHandler
  fun on(event: OrganizationDefinedEvent) {
    logger.info { "Projecting OrganizationDefinedEvent for: ${event.organizationName}" }
    val entity =
            repository.findById(event.organizationId).orElseGet {
              OrganizationListReadModelEntity()
            }
    entity
            .apply {
              organizationId = event.organizationId
              organizationName = event.organizationName
              personId = event.personId
              username = event.username
              role = event.role
            }
            .also { repository.save(it) }
  }
}
