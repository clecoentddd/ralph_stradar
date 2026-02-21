package administration.admin.companylistlookup.internal

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity
import administration.events.ListOfCompaniesFetchedEvent
import java.time.Instant
import mu.KotlinLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Repository
interface CompanyListLookUpReadModelRepository :
        JpaRepository<CompanyListLookUpReadModelEntity, Long> {

  // Add this to allow clearing the old list for a specific settings profile
  fun deleteBySettingsId(settingsId: java.util.UUID)
}

@Component
class CompanyListLookUpReadModelProjector(
        private val repository: CompanyListLookUpReadModelRepository
) {

  @ResetHandler
  fun reset() {
    repository.deleteAllInBatch() // Clear the table before replaying
    logger.info { "Resetting CompanyListLookUpReadModelProjector" }
  }

  @EventHandler
  @Transactional
  fun on(event: ListOfCompaniesFetchedEvent, @Timestamp axonTimestamp: Instant) {
    // 1. Clear old data for this settingsId
    repository.deleteBySettingsId(event.settingsId)
    logger.info { "Deleting old companies for settingsId: ${event.settingsId}" }

    // 2. Map the list directly (no null check needed based on your warning)
    val entities =
            event.listOfCompanies.map { details ->
              CompanyListLookUpReadModelEntity().apply {
                this.companyId = details.companyId
                this.companyName = details.companyName
                this.settingsId = event.settingsId
                this.connectionId = event.connectionId
                this.timestamp = axonTimestamp.toEpochMilli()
              }
            }

    // 3. Save all new rows in one batch
    repository.saveAll(entities)
  }
}
