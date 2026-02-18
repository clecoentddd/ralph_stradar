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
  fun handleQuery(query: ClientAccountListReadModelQuery): ClientAccountListReadModel? {
    return ClientAccountListReadModel(repository.findAll())
  }
}
