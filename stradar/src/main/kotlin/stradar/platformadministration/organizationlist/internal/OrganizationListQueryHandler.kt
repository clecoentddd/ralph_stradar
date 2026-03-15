package stradar.platformadministration.organizationlist.internal

import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.platformadministration.organizationlist.OrganizationListReadModel
import stradar.platformadministration.organizationlist.OrganizationListReadModelQuery

@Component
class OrganizationListQueryHandler(private val repository: OrganizationListReadModelRepository) {

  @QueryHandler
  fun handleQuery(query: OrganizationListReadModelQuery): OrganizationListReadModel {
    // Returns the full list for the admin view
    return OrganizationListReadModel(repository.findAll())
  }
}
