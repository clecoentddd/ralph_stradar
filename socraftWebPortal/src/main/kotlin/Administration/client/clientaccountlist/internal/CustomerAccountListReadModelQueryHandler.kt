package administration.client.clientaccountlist.internal

import administration.client.clientaccountlist.ClientAccountListReadModel
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
  fun handleQuery(query: ClientAccountListReadModelQuery): List<ClientAccountListReadModel> {
    return when {
      query.email != null && query.companyId != null -> {
        repository.findByClientEmailAndCompanyId(query.email, query.companyId)?.let { listOf(it) }
                ?: emptyList()
      }
      query.email != null -> {
        repository.findByClientEmail(query.email)?.let { listOf(it) } ?: emptyList()
      }
      else -> {
        repository.findAll()
      }
    }
  }
}
