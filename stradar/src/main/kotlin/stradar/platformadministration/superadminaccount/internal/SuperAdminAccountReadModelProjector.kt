package stradar.platformadministration.superadminaccount.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import stradar.events.SuperAdminSignedInEvent // 👈 Use the Admin event, not Organization
import stradar.platformadministration.ProcessingGroups
import stradar.platformadministration.superadminaccount.SuperAdminAccountReadModelEntity

interface SuperAdminAccountReadModelRepository :
    JpaRepository<SuperAdminAccountReadModelEntity, UUID> {
  fun existsByUsername(username: String): Boolean
}

@Component
@ProcessingGroup(ProcessingGroups.PLATFORM_ADMINISTRATION)
class SuperAdminAccountListReadModelProjector(
    private val repository: SuperAdminAccountReadModelRepository
) {

  private val logger = KotlinLogging.logger {}

  @EventHandler
  fun on(event: SuperAdminSignedInEvent) {
    logger.info { "Projecting SuperAdmin entry for: ${event.username}" }

    // Find existing by the Admin ID or create new
    val entity =
        repository.findById(event.adminAccountId).orElseGet { SuperAdminAccountReadModelEntity() }

    entity.apply {
      this.adminAccountId = event.adminAccountId
      this.username = event.username
    }

    repository.save(entity)
  }
}
