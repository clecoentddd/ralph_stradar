package administration.admin.systemstatus.internal

import administration.admin.systemstatus.SystemStatusReadModelQuery
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659756306810
*/
@Component
class SystemStatusReadModelQueryHandler(private val repository: SystemStatusReadModelRepository) {

  @QueryHandler
  fun handleQuery(query: SystemStatusReadModelQuery): Boolean {
    // We simply return true if the record exists in the database
    return repository.existsById(query.settingsId)
  }
}
