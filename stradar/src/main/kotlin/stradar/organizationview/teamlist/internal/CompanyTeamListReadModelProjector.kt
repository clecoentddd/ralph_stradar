package stradar.organizationview.teamlist.internal

import java.util.UUID
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import stradar.events.TeamCreatedEvent
import stradar.organizationview.ProcessingGroups
import stradar.organizationview.teamlist.CompanyTeamListReadModelEntity

interface CompanyTeamListReadModelRepository : JpaRepository<CompanyTeamListReadModelEntity, UUID> {
    fun existsByOrganizationIdAndName(organizationId: UUID, name: String): Boolean
}

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645849750300
*/
@Component
@ProcessingGroup(ProcessingGroups.COMPANY_VIEW)
class CompanyTeamListReadModelProjector(var repository: CompanyTeamListReadModelRepository) {

    @EventHandler
    fun on(event: TeamCreatedEvent) {
        // throws exception if not available (adjust logic)
        val entity = this.repository.findById(event.teamId).orElse(CompanyTeamListReadModelEntity())
        entity
                .apply {
                    teamId = event.teamId
                    context = event.context
                    level = event.level
                    name = event.name
                    organizationId = event.organizationId
                    organizationName = event.organizationName
                    adminAccountId = event.adminAccountId
                    purpose = event.purpose
                }
                .also { this.repository.save(it) }
    }
}
