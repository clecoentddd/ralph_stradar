package administration.admin.companylistlookup.internal

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity
import administration.events.ListOfCompaniesFetchedEvent
import java.time.Instant
import java.util.UUID
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface CompanyListLookUpReadModelRepository :
    JpaRepository<CompanyListLookUpReadModelEntity, UUID>

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822859
*/

@Component
class CompanyListLookUpReadModelProjector(var repository: CompanyListLookUpReadModelRepository) {

  @EventHandler
  @Transactional
  fun on(
      event: ListOfCompaniesFetchedEvent,
      @Timestamp axonTimestamp: Instant // Injected by Axon
  ) {

    // 1. Find the existing single record for this settingsId or create a new one
    // Because settingsId is the @Id, findById is the correct way to locate the row
    val entity =
        repository.findById(event.settingsId).orElseGet {
          CompanyListLookUpReadModelEntity(settingsId = event.settingsId)
        }

    // 2. Update the record
    entity.apply {
      this.connectionId = event.connectionId
      this.listOfCompanies = event.listOfCompanies
      this.timestamp = axonTimestamp.toEpochMilli()
    }

    // 3. Save (this will Update the existing row or Insert if it was new)
    repository.save(entity)
  }
}
