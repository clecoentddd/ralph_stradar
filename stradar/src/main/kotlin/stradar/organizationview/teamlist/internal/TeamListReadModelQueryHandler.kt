package stradar.organizationview.teamlist.internal

import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.organizationview.teamlist.*

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645849750300
*/
@Component
class TeamListReadModelQueryHandler(private val repository: TeamListReadModelRepository) {

  @QueryHandler
  fun handleQuery(_query: TeamListReadModelQuery): TeamListReadModel? {
    return TeamListReadModel(repository.findAll())
  }

  @QueryHandler
  fun handleByOrganization(query: TeamListByOrganizationQuery): TeamListReadModel {
    return TeamListReadModel(repository.findByOrganizationId(query.organizationId))
  }

  @QueryHandler
  fun handleUniqueness(query: TeamNameUniquenessQuery): Boolean {
    return repository.existsByOrganizationIdAndName(query.organizationId, query.teamName)
  }

  /**
   * * NEW: Step-by-step addition to handle fetching Team Name by ID. We include MetaData as a
   * parameter now so it's ready for your organization check.
   */
  @QueryHandler
  fun handle(query: TeamNameByTeamIdQuery, metadata: MetaData): TeamNameResponse {
    val team =
            repository.findById(query.teamId).orElseThrow {
              IllegalStateException("Team with ID ${query.teamId} not found")
            }

    // Here is where you will later add:
    // if (team.organizationId != metadata["organizationId"]) { ... }

    return TeamNameResponse(teamName = team.name ?: "Unknown Team")
  }
}
