package administration.client.companyinvoiceslookup.internal

import administration.client.companyinvoiceslookup.InvoiceListReadModelEntity
import administration.client.companyinvoiceslookup.InvoiceListReadModelRepository
import administration.events.InvoicesFetchedEvent
import java.time.Instant
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660086962398
*/
@Component
class InvoiceListReadModelProjector(private val repository: InvoiceListReadModelRepository) {

    @EventHandler
    @Transactional
    fun on(event: InvoicesFetchedEvent, @Timestamp axonTimestamp: Instant) {

        // 1. Find existing record by companyId or create a new shell instance
        val entity =
                repository.findById(event.companyId).orElseGet {
                    InvoiceListReadModelEntity().apply { this.companyId = event.companyId }
                }

        // 2. Update the state (Mapped to JSONB column)
        entity.apply {
            this.clientId = event.clientId
            this.invoiceList = event.invoiceList // List<ListOfInvoicesItem>
            this.timestamp = axonTimestamp.toEpochMilli()
        }

        // 3. Persist (Save replaces the single row for this companyId)
        repository.save(entity)
    }
}
