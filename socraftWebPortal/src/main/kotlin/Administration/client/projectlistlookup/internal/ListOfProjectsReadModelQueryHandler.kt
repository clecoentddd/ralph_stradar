package administration.client.projectlistlookup.internal

import administration.client.projectlistlookup.ListOfProjectsReadModelEntity
import administration.client.projectlistlookup.ListOfProjectsReadModelQuery
import administration.client.projectlistlookup.ListOfProjectsReadModelRepository
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660065978340
*/

@Component
class ListOfProjectsReadModelQueryHandler(
        private val repository: ListOfProjectsReadModelRepository
) {

  @QueryHandler
  fun handleQuery(query: ListOfProjectsReadModelQuery): ListOfProjectsReadModelEntity? {
    // We find the entity by ID.
    // If it exists, Optional.orElse(null) returns the entity.
    // If it doesn't, it returns null.
    return repository.findById(query.companyId).orElse(null)
  }
}
