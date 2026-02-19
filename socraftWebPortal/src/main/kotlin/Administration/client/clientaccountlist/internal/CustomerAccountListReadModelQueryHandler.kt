package administration.client.clientaccountlist.internal

import administration.client.clientaccountlist.ClientAccountListReadModelEntity
import administration.client.clientaccountlist.ClientAccountListReadModelQuery
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660036569910
*/
@Component
class ClientAccountListReadModelQueryHandler(
        private val repository: ClientAccountListReadModelRepository
) {

  @QueryHandler
  fun handleQuery(query: ClientAccountListReadModelQuery): List<ClientAccountListReadModelEntity> {
    // We return the List directly instead of wrapping it in ClientAccountListReadModel
    return if (query.email != null) {
      // Fetch only the specific record for uniqueness check
      repository.findByClientEmail(query.email)?.let { listOf(it) } ?: emptyList()
    } else {
      // Standard behavior: fetch everything for the dashboard
      repository.findAll()
    }
  }
}
