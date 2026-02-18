package administration.client.companyorderlistlookup.internal

import administration.client.companyorderlistlookup.ListOfOrdersReadModel
import administration.client.companyorderlistlookup.ListOfOrdersReadModelQuery
import administration.client.companyorderlistlookup.ListOfOrdersReadModelRepository
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660085256087
*/
@Component
class ListOfOrdersReadModelQueryHandler(private val repository: ListOfOrdersReadModelRepository) {

  @QueryHandler
  fun handleQuery(query: ListOfOrdersReadModelQuery): ListOfOrdersReadModel? {
    // Optimized to perform only one DB query instead of existsById + findById
    return repository
            .findById(query.companyId)
            .map { entity -> ListOfOrdersReadModel(entity) }
            .orElse(null)
  }
}
