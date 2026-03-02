package stradar.organizationview.strategies.internal

import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.organizationview.strategies.GetStrategiesByOrganizationQuery
import stradar.organizationview.strategies.GetStrategiesByTeamQuery
import stradar.organizationview.strategies.StrategiesReadModel

/*
Boardlink:
https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661684920684
*/

@Component
class StrategiesReadModelQueryHandler(private val repository: StrategiesReadModelRepository) {

  /** Get strategies by organization */
  @QueryHandler
  fun handle(query: GetStrategiesByOrganizationQuery): StrategiesReadModel {
    return StrategiesReadModel(repository.findAllByOrganizationId(query.organizationId))
  }

  /** Get strategies by team */
  @QueryHandler
  fun handle(query: GetStrategiesByTeamQuery): StrategiesReadModel {
    return StrategiesReadModel(repository.findAllByTeamId(query.teamId))
  }
}
