package stradar.organizationview.environmentalchanges.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.events.EnvironmentalChangeDeletedEvent
import stradar.events.EnvironmentalChangeDetectedEvent
import stradar.events.EnvironmentalChangeUpdatedEvent
import stradar.organizationview.ProcessingGroups
import stradar.organizationview.environmentalchanges.EnvironmentalChangesReadModelEntity

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661040894563
*/

interface EnvironmentalChangesReadModelRepository :
        JpaRepository<EnvironmentalChangesReadModelEntity, UUID> {
        fun findByEnvironmentalChangeId(
                environmentalChangeId: UUID
        ): List<EnvironmentalChangesReadModelEntity>
        fun findByEnvironmentalChangeIdAndOrganizationId(
                environmentalChangeId: UUID,
                organizationId: UUID
        ): List<EnvironmentalChangesReadModelEntity>
        fun findByTeamId(teamId: UUID): List<EnvironmentalChangesReadModelEntity>
        fun findByTeamIdAndOrganizationId(
                teamId: UUID,
                organizationId: UUID
        ): List<EnvironmentalChangesReadModelEntity>
}

@ProcessingGroup(ProcessingGroups.COMPANY_VIEW)
@Component
class EnvironmentalChangesReadModelProjector(
        var repository: EnvironmentalChangesReadModelRepository
) {

        private val logger = KotlinLogging.logger {}

        @EventHandler
        fun on(event: EnvironmentalChangeDetectedEvent, metaData: MetaData) {
                logger.info { "Projecting New Flat Element: ${event.environmentalChangeId}" }

                val secureOrgId = metaData.resolveOrganizationId()

                val entity =
                        EnvironmentalChangesReadModelEntity().apply {
                                this.environmentalChangeId = event.environmentalChangeId
                                this.teamId = event.teamId
                                this.organizationId = secureOrgId
                                this.title = event.title
                                this.detect = event.detect
                                this.assess = event.assess
                                this.respond = event.respond
                                this.type = event.type
                                this.category = event.category
                                this.distance = event.distance
                                this.impact = event.impact
                                this.risk = event.risk
                        }

                repository.save(entity)
        }

        @EventHandler
        fun on(event: EnvironmentalChangeUpdatedEvent) {
                logger.info { "Updating Flat Element: ${event.environmentalChangeId}" }

                val entity =
                        repository.findById(event.environmentalChangeId).orElseThrow {
                                IllegalArgumentException(
                                        "Environmental change not found for id: ${event.environmentalChangeId}"
                                )
                        }

                entity.apply {
                        this.title = event.title
                        this.detect = event.detect
                        this.assess = event.assess
                        this.respond = event.respond
                        this.type = event.type
                        this.category = event.category
                        this.distance = event.distance
                        this.impact = event.impact
                        this.risk = event.risk
                }

                repository.save(entity)
        }

        @EventHandler
        fun on(event: EnvironmentalChangeDeletedEvent) {
                logger.info { "Deleting Flat Element: ${event.environmentalChangeId}" }

                if (repository.existsById(event.environmentalChangeId)) {
                        repository.deleteById(event.environmentalChangeId)
                }
        }
}
