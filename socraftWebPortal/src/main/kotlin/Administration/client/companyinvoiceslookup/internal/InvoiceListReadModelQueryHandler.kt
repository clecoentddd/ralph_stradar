package administration.client.companyinvoiceslookup.internal

import administration.client.companyinvoiceslookup.InvoiceListReadModel
import administration.client.companyinvoiceslookup.InvoiceListReadModelQuery
import administration.client.companyinvoiceslookup.InvoiceListReadModelRepository
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660086962398
*/
@Component
class InvoiceListReadModelQueryHandler(private val repository: InvoiceListReadModelRepository) {

  @QueryHandler
  fun handleQuery(query: InvoiceListReadModelQuery): InvoiceListReadModel? {
    // One DB round-trip is better than two.
    // We find the entity and map it directly to the wrapper if it exists.
    return repository
            .findById(query.companyId)
            .map { entity -> InvoiceListReadModel(entity) }
            .orElse(null)
  }
}
