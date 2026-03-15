package stradar.organizationview.initiativelist.internal

import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.organizationview.initiativelist.AllInitiativesForOrganizationQuery
import stradar.organizationview.initiativelist.InitiativeListResponse
import stradar.organizationview.initiativelist.InitiativesByStrategyQuery
import stradar.organizationview.initiativelist.InitiativesReadModel
import stradar.organizationview.initiativelist.InitiativesReadModelQuery
import stradar.organizationview.initiativelist.OrganizationInitiativeListResponse

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645855652122
*/
@Component
class InitiativesReadModelQueryHandler(private val repository: InitiativesReadModelRepository) {

  /**
   * * Handles the high-level Organization Dashboard lookup. Groups initiatives by Team and then by
   *   Level (Step).
   */
  @QueryHandler
  fun handle(query: AllInitiativesForOrganizationQuery): OrganizationInitiativeListResponse {
    // 1. Fetch the data
    val results =
        repository.findAllByOrganizationId(query.organizationId).filter { it.statut != "Deleted" }

    // 2. Return the exact type the Controller is expecting
    // We use a dummy UUID for strategyId because the Linker modal doesn't need it
    return OrganizationInitiativeListResponse(
        organizationId = query.organizationId, items = results)
  }
  /** Handles the single initiative lookup by ID */
  @QueryHandler
  fun handle(query: InitiativesReadModelQuery): InitiativesReadModel {
    val entity =
        repository.findById(query.initiativeId).orElseThrow {
          NoSuchElementException("Initiative not found: ${query.initiativeId}")
        }

    return InitiativesReadModel(
        data =
            entity.apply {
              allItems.size // Force lazy-load the collection while
              // session is active
            })
  }

  /** Handles lookup by Strategy/Team context (Traditional Strategy view) */
  @QueryHandler
  fun handle(query: InitiativesByStrategyQuery): InitiativeListResponse {
    val results =
        repository
            .findAllByStrategyIdAndTeamIdAndOrganizationId(
                query.strategyId, query.teamId, query.organizationId)
            .filter { it.statut != "Deleted" }

    return InitiativeListResponse(strategyId = query.strategyId, items = results)
  }
}
