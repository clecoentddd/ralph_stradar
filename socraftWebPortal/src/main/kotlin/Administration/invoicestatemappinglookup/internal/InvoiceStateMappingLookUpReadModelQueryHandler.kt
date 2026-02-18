package administration.invoicestatemappinglookup.internal

import administration.invoicestatemappinglookup.InvoiceStateMappingLookUpReadModelEntity
import administration.invoicestatemappinglookup.InvoiceStateMappingLookUpReadModelQuery
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713541
*/
@Component
class InvoiceStateMappingLookUpReadModelQueryHandler(
        private val repository: InvoiceStateMappingLookUpReadModelRepository
) {

  @QueryHandler
  fun handleQuery(
          query: InvoiceStateMappingLookUpReadModelQuery
  ): InvoiceStateMappingLookUpReadModelEntity? {
    return repository.findById(query.settingsId).orElse(null)
  }
}
