package stradar.organizationview.initiativelist.internal

import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.organizationview.initiativelist.InitiativeListResponse
import stradar.organizationview.initiativelist.InitiativesByStrategyQuery
import stradar.organizationview.initiativelist.InitiativesReadModel
import stradar.organizationview.initiativelist.InitiativesReadModelQuery

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645855652122
*/
@Component
class InitiativesReadModelQueryHandler(private val repository: InitiativesReadModelRepository) {

  /** Handles the single initiative lookup by ID */
  @QueryHandler
  fun handle(query: InitiativesReadModelQuery): InitiativesReadModel {
    val entity = repository.findById(query.initiativeId).get()

    // Accessing the fields here "touches" them, forcing a load
    // while the QueryHandler's session is still active.
    return InitiativesReadModel(
            data =
                    entity.apply {
                      diagnostic.size // Force load
                      overallPlan.size // Force load
                    }
    )
  }

  /**
   * Handles the coherent lookup by Strategy, Team, and Organization. This ensures users only see
   * what they are authorized to see.
   */
  @QueryHandler
  fun handle(query: InitiativesByStrategyQuery): InitiativeListResponse {
    val results =
            repository.findAllByStrategyIdAndTeamIdAndOrganizationId(
                    query.strategyId,
                    query.teamId,
                    query.organizationId
            )

    return InitiativeListResponse(strategyId = query.strategyId, items = results)
  }
}
