package administration.admin.companylistlookup.internal

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity // Returning entity
import administration.admin.companylistlookup.CompanyListLookUpReadModelQuery
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822859
*/
@Component
class CompanyListLookUpReadModelQueryHandler(
        private val repository: CompanyListLookUpReadModelRepository
) {

  @QueryHandler
  fun handleQuery(query: CompanyListLookUpReadModelQuery): CompanyListLookUpReadModelEntity? {
    // Since settingsId is the @Id, we use the standard findById.
    // This is more efficient than calling exists() then findById().
    return repository.findById(query.settingsId).orElse(null)
  }
}
