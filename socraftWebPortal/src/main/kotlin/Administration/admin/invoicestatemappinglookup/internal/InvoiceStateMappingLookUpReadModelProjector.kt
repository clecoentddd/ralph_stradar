package administration.admin.invoicestatemappinglookup.internal

import administration.admin.invoicestatemappinglookup.InvoiceStateMappingLookUpReadModelEntity
import administration.events.InvoiceStateMappingFetchedEvent
import java.time.Instant
import java.util.UUID
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface InvoiceStateMappingLookUpReadModelRepository :
    JpaRepository<InvoiceStateMappingLookUpReadModelEntity, UUID>

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713541
*/

@Component
class InvoiceStateMappingLookUpReadModelProjector(
    var repository: InvoiceStateMappingLookUpReadModelRepository
) {

  @EventHandler
  @Transactional // Good practice to ensure the find-and-save happens in one transaction
  fun on(
      event: InvoiceStateMappingFetchedEvent,
      @Timestamp axonTimestamp: Instant // Injected by Axon
  ) {

    // Find existing record by settingsId (the PK) or create a new empty instance
    val entity =
        this.repository
            .findById(event.settingsId)
            .orElse(InvoiceStateMappingLookUpReadModelEntity())

    entity.apply {
      this.settingsId = event.settingsId
      this.connectionId = event.connectionId
      // This is now saved as a single JSONB column in the DB row
      this.listOfInvoiceStates = event.listOfInvoiceStates
      this.timestamp = axonTimestamp.toEpochMilli()
    }

    this.repository.save(entity)
  }
}
