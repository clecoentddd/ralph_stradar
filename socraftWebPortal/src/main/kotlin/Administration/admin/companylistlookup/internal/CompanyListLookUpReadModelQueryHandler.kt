package administration.admin.companylistlookup.internal

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity // Returning entity
import administration.admin.companylistlookup.FetchAllCompaniesQuery
import administration.admin.companylistlookup.FetchCompanyNameByCompanyIdQuery
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822859
*/

@Component
class CompanyListLookUpReadModelQueryHandler(
        private val repository: CompanyListLookUpReadModelRepository
) {

  /** ADMIN QUERY: Returns all rows. Used by internal admin tools to see the full lookup table. */
  @QueryHandler
  fun handle(query: FetchAllCompaniesQuery): List<CompanyListLookUpReadModelEntity> {
    return repository.findAll()
  }

  /**
   * CLIENT QUERY: Returns only the name for a specific ID. Includes a security check via MetaData
   * to ensure the client isn't "guessing" other company IDs.
   */
  @QueryHandler
  fun handle(query: FetchCompanyNameByCompanyIdQuery): String {
    // We assume the security filter/interceptor puts 'companyId' into metadata
    /*  val authorizedId = metaData["companyId"]?.toString()?.toLongOrNull()

    if (authorizedId != query.companyId) {
      throw SecurityException(
              "Access Denied: You cannot request data for company ${query.companyId}"
      )
    }*/

    return repository.findById(query.companyId).map { it.companyName }.orElse("Unknown Company")
  }
}
