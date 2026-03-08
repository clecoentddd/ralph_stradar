package stradar.organizationview.teamlist.internal

import java.util.UUID
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import stradar.events.TeamCreatedEvent
import stradar.events.TeamDeletedEvent
import stradar.events.TeamUpdatedEvent
import stradar.organizationview.ProcessingGroups
import stradar.organizationview.teamlist.TeamListReadModelEntity

interface TeamListReadModelRepository : JpaRepository<TeamListReadModelEntity, UUID> {
    // Used for the Multi-Tenant API: Only return teams for the user's tenant
    fun findByOrganizationIdAndStatus(
            organizationId: UUID,
            status: String = "ACTIVE"
    ): List<TeamListReadModelEntity>

    fun existsByNameAndOrganizationId(name: String, organizationId: UUID): Boolean
}

@Component
@ProcessingGroup(ProcessingGroups.COMPANY_VIEW)
class TeamListReadModelProjector(var repository: TeamListReadModelRepository) {

    @EventHandler
    fun on(event: TeamCreatedEvent) {
        // Create the entry for the specific tenant
        val entity =
                TeamListReadModelEntity().apply {
                    this.teamId = event.teamId
                    this.organizationId = event.organizationId // Permanent Tenant ID
                    this.context = event.context
                    this.level = event.level
                    this.name = event.name
                    this.purpose = event.purpose
                    this.status = "ACTIVE"
                }
        repository.save(entity)
    }

    @EventHandler
    fun on(event: TeamUpdatedEvent) {
        // Find the team within the projection
        repository.findById(event.teamId).ifPresent { entity ->
            entity.apply {
                // We update the data, but we NEVER change the organizationId (Tenant ID)
                this.context = event.context
                this.level = event.level
                this.name = event.name
                this.purpose = event.purpose
                this.status = event.status ?: "ACTIVE"
            }
            repository.save(entity)
        }
    }

    @EventHandler
    fun on(event: TeamDeletedEvent) {
        // Soft-delete the team so it disappears from the "Active" tenant view
        repository.findById(event.teamId).ifPresent { entity ->
            entity.status = "DELETED"
            entity.reason = event.reason
            repository.save(entity)
        }
    }
}
