package stradar.organizationview.initiativelist.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
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
}

@Component
class InitiativesReadModelProjector(private val repository: InitiativesReadModelRepository) {

  @EventHandler
  fun on(event: InitiativeCreatedEvent) {
    logger.info { "Projecting new initiative: ${event.initiativeId}" }
    val entity =
            InitiativesReadModelEntity().apply {
              initiativeId = event.initiativeId
              initiativeName = event.initiativeName
              organizationId = event.organizationId
              strategyId = event.strategyId
              teamId = event.teamId
            }
    repository.save(entity)
  }

  @EventHandler
  fun on(event: InitiativeItemChangedEvent) {
    val initiative =
            repository.findById(event.initiativeId).orElseThrow {
              IllegalStateException("Initiative ${event.initiativeId} not found")
            }

    // 1. Explicitly cast or ensure targetList is Mutable
    val targetList: MutableList<InitiativeItem> =
            when (event.step.uppercase()) {
              "DIAGNOSTIC" -> initiative.diagnostic
              "OVERALLAPPROACH" -> initiative.overallPlan
              "COHERENTACTION" -> initiative.coherentActions
              "PROXIMATEOBJECTIVE" -> initiative.proximateObjectives
              else -> throw IllegalArgumentException("Unknown step: ${event.step}")
            }

    // 2. Use removeIf (standard Java/Kotlin 1.1+ feature for MutableIterables)
    targetList.removeIf { it.id == event.itemId }

    // 3. Re-add if not a deletion
    if (event.status != "DELETED") {
      targetList.add(
              InitiativeItem(id = event.itemId, content = event.content, status = event.status)
      )
    }

    repository.save(initiative)
  }
}
