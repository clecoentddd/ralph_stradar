package stradar.organizationview.strategies.internal

import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.organizationview.strategies.GetStrategiesByOrganizationQuery
import stradar.organizationview.strategies.GetStrategiesByTeamQuery
import stradar.organizationview.strategies.StrategiesReadModel

/*
Boardlink:
https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661684920684
*/

@Component
class StrategiesReadModelQueryHandler(private val repository: StrategiesReadModelRepository) {

  /** Get strategies by organization — org enforced from MetaData, not from the query object */
  @QueryHandler
  fun handle(query: GetStrategiesByOrganizationQuery, metaData: MetaData): StrategiesReadModel {
    val organizationId = metaData.resolveOrganizationId()
    return StrategiesReadModel(repository.findAllByOrganizationId(organizationId))
  }

  @QueryHandler
  fun handle(query: GetStrategiesByTeamQuery, metaData: MetaData): StrategiesReadModel {
    val organizationId = metaData.resolveOrganizationId()

    // Scoped repository call ensures data isolation
    val results = repository.findAllByTeamIdAndOrganizationId(query.teamId, organizationId)

    return StrategiesReadModel(results)
  }
}
