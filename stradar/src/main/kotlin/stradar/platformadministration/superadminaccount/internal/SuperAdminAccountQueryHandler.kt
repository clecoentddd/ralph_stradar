package stradar.platformadministration.superadminaccountlist.internal

import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.platformadministration.superadminaccount.SuperAdminAccountReadModel
import stradar.platformadministration.superadminaccount.SuperAdminAccountReadModelQuery
import stradar.platformadministration.superadminaccount.internal.SuperAdminAccountReadModelRepository

@Component
class SuperAdminAccountListQueryHandler(
    private val repository: SuperAdminAccountReadModelRepository
) {

  @QueryHandler
  fun handleQuery(query: SuperAdminAccountReadModelQuery): SuperAdminAccountReadModel {
    // Returns the full list for the admin view
    return SuperAdminAccountReadModel(repository.findAll())
  }
}
