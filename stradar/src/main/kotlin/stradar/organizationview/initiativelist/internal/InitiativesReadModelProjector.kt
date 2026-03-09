package stradar.organizationview.initiativelist.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.events.InitiativeCreatedEvent
import stradar.events.InitiativeItemChangedEvent
import stradar.organizationview.initiativelist.InitiativeItem
import stradar.organizationview.initiativelist.InitiativesReadModelEntity

private val logger = KotlinLogging.logger {}

interface InitiativesReadModelRepository : JpaRepository<InitiativesReadModelEntity, UUID> {

        // This is the 3-parameter query we discussed for "coherence"

        fun findAllByStrategyIdAndTeamIdAndOrganizationId(
                strategyId: UUID,
                teamId: UUID,
                organizationId: UUID
        ): List<InitiativesReadModelEntity>

        fun findAllByOrganizationId(organizationId: UUID): List<InitiativesReadModelEntity>
}

@Component
class InitiativesReadModelProjector(private val repository: InitiativesReadModelRepository) {

        @EventHandler
        fun on(event: InitiativeCreatedEvent, metaData: MetaData) {
                logger.info { "Projecting new initiative: ${event.initiativeId}" }
                val secureOrgId = metaData.resolveOrganizationId()
                val entity =
                        InitiativesReadModelEntity().apply {
                                initiativeId = event.initiativeId
                                initiativeName = event.initiativeName
                                organizationId = secureOrgId
                                strategyId = event.strategyId
                                teamId = event.teamId
                        }
                repository.save(entity)
        }

        @EventHandler
        fun on(event: InitiativeItemChangedEvent, metaData: MetaData) {
                val secureOrgId = metaData.resolveOrganizationId()
                val initiative =
                        repository.findById(event.initiativeId).orElseThrow {
                                IllegalStateException("Initiative ${event.initiativeId} not found")
                        }

                if (initiative.organizationId != secureOrgId) {
                        throw IllegalAccessException(
                                "Security context mismatch for Initiative Projector"
                        )
                }

                // 1. Remove the item if it exists (using standard Java/Kotlin removeIf)
                initiative.allItems.removeIf { it.id == event.itemId }

                // 2. Re-add if not a deletion
                if (event.status != "DELETED") {
                        initiative.allItems.add(
                                InitiativeItem(
                                        id = event.itemId,
                                        content = event.content,
                                        status = event.status,
                                        step = event.step.uppercase()
                                )
                        )
                }

                repository.save(initiative)
        }
}
