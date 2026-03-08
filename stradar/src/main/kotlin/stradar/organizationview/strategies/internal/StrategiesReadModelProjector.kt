package stradar.organizationview.strategies.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.events.StrategyDraftCreatedEvent
import stradar.organizationview.strategies.StrategiesReadModelEntity

/** Repository for the strategy projection */
interface StrategiesReadModelRepository : JpaRepository<StrategiesReadModelEntity, UUID> {

    fun findAllByOrganizationId(organizationId: UUID): List<StrategiesReadModelEntity>

    fun findAllByTeamIdAndOrganizationId(
            teamId: UUID,
            organizationId: UUID
    ): List<StrategiesReadModelEntity>
}

/*
Boardlink:
https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661684920684
*/

private val logger = KotlinLogging.logger {}

@Component
class StrategiesReadModelProjector(private val repository: StrategiesReadModelRepository) {

    /**
     * When a strategy draft is created → we insert or update the strategy row.
     *
     * This projection is idempotent and safe for event replay.
     */
    @EventHandler
    fun on(event: StrategyDraftCreatedEvent, metaData: MetaData) {
        val secureOrgId = metaData.resolveOrganizationId()

        val entity = repository.findById(event.strategyId).orElse(StrategiesReadModelEntity())

        entity.apply {
            strategyId = event.strategyId
            strategyBuilderId = event.strategyBuilderId
            organizationId = secureOrgId
            teamId = event.teamId
            strategyName = event.strategyName
            strategyTimeframe = event.strategyTimeframe
            status = "DRAFT"
        }

        repository.save(entity)
    }
}
