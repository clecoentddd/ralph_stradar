package administration.admin.companylistlookup.internal

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity
import administration.events.ListOfCompaniesFetchedEvent
import java.time.Instant
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CompanyListLookUpReadModelRepository :
        JpaRepository<CompanyListLookUpReadModelEntity, Long>

@Component
class CompanyListLookUpReadModelProjector(
        private val repository: CompanyListLookUpReadModelRepository
) {

  @ResetHandler
  fun reset() {
    repository.deleteAllInBatch() // Clear the table before replaying
  }

  @EventHandler
  @Transactional
  fun on(event: ListOfCompaniesFetchedEvent, @Timestamp axonTimestamp: Instant) {
    // Flatten the incoming JSON list into individual database rows
    event.listOfCompanies?.forEach { details ->

      // 1. Find existing company by its Int ID or create a new instance
      val entity =
              repository.findById(details.companyId).orElseGet {
                CompanyListLookUpReadModelEntity().apply { this.companyId = details.companyId }
              }

      // 2. Update the fields for this specific company row
      entity.apply {
        this.companyName = details.companyName
        this.settingsId = event.settingsId
        this.connectionId = event.connectionId
        this.timestamp = axonTimestamp.toEpochMilli()
      }

      // 3. Save the individual row
      repository.save(entity)
    }
  }
}
