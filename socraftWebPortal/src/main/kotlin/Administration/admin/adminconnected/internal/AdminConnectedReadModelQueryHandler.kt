package administration.admin.adminconnected.internal

import administration.admin.adminconnected.AdminConnectedReadModel
import administration.admin.adminconnected.AdminConnectedReadModelQuery
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822173
*/
@Component
class AdminConnectedReadModelQueryHandler(
    private val repository: AdminConnectedReadModelRepository
) {

  @QueryHandler
  fun handleQuery(query: AdminConnectedReadModelQuery): AdminConnectedReadModel? {

    val result = repository.findById(query.connectionId)
    return if (result.isPresent) result.get() else null
  }
}
