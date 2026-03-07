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
    fun findByOrganizationId(organizationId: UUID): List<TeamListReadModelEntity>
    fun existsByNameAndOrganizationId(name: String, organizationId: UUID): Boolean
}

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645849750300
*/
@Component
@ProcessingGroup(ProcessingGroups.COMPANY_VIEW)
class TeamListReadModelProjector(var repository: TeamListReadModelRepository) {

    @EventHandler
    fun on(event: TeamCreatedEvent) {
        // throws exception if not available (adjust logic)
        val entity = this.repository.findById(event.teamId).orElse(TeamListReadModelEntity())
        entity
                .apply {
                    teamId = event.teamId
                    context = event.context
                    level = event.level
                    name = event.name
                    organizationId = event.organizationId
                    purpose = event.purpose
                }
                .also { this.repository.save(it) }
    }

    @EventHandler
    fun on(event: TeamDeletedEvent) {
        this.repository.deleteById(event.teamId)
    }

    @EventHandler
    fun on(event: TeamUpdatedEvent) {
        // 🛡️ Change orElseThrow to orElse(TeamListReadModelEntity())
        val entity = this.repository.findById(event.teamId).orElse(TeamListReadModelEntity())

        entity
                .apply {
                    // Essential: Set the IDs in case this is a NEW insertion (Recovery)
                    this.teamId = event.teamId
                    this.organizationId = event.organizationId

                    // Update the fields
                    this.context = event.context
                    this.level = event.level
                    this.name = event.name
                    this.purpose = event.purpose
                }
                .also { this.repository.save(it) }
    }
}
