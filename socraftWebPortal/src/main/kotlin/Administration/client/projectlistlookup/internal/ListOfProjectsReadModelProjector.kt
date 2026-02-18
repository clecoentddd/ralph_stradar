package administration.client.projectlistlookup.internal

// repository
import administration.client.projectlistlookup.ListOfProjectsReadModelEntity
import administration.client.projectlistlookup.ListOfProjectsReadModelRepository // Import the
import administration.events.ListOfProjectsFetchedEvent
import java.time.Instant
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660065978340
*/

@Component
class ListOfProjectsReadModelProjector(
        private val repository:
                ListOfProjectsReadModelRepository // Fix: Inject Repository, not Entity
) {

    @EventHandler
    @Transactional
    fun on(event: ListOfProjectsFetchedEvent, @Timestamp axonTimestamp: Instant) {

        // 1. Find existing record or create new
        val entity =
                repository.findById(event.companyId).orElseGet {
                    ListOfProjectsReadModelEntity().apply { this.companyId = event.companyId }
                }

        // 2. Update the state
        entity.apply {
            this.clientId = event.clientId
            this.companyName = "TO BE ADDED"
            // This line (38) will now resolve correctly
            this.projectList = event.projectList
            this.timestamp = axonTimestamp.toEpochMilli()
        }

        // 3. Save
        repository.save(entity)
    }
}
