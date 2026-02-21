package administration.client.projectlistlookup.internal

import administration.client.projectlistlookup.ListOfProjectsReadModelEntity
import administration.client.projectlistlookup.ListOfProjectsReadModelRepository
import administration.events.ListOfProjectsFetchedEvent
import java.time.Instant
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ListOfProjectsReadModelProjector(private val repository: ListOfProjectsReadModelRepository) {

  @EventHandler
  @Transactional
  fun on(event: ListOfProjectsFetchedEvent, @Timestamp axonTimestamp: Instant) {

    // 1. Find existing record or create new
    val entity =
            repository.findById(event.companyId).orElseGet {
              ListOfProjectsReadModelEntity().apply { this.companyId = event.companyId }
            }

    // 2. Update the state (Company Name is now handled by the UI lookup)
    entity.apply {
      this.clientId = event.clientId
      this.projectList = event.projectList
      this.timestamp = axonTimestamp.toEpochMilli()
    }

    // 3. Save
    repository.save(entity)
  }
}
