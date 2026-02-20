package administration.client.companyorderlistlookup.internal

import administration.client.companyorderlistlookup.ListOfOrdersReadModelEntity
import administration.client.companyorderlistlookup.ListOfOrdersReadModelRepository
import administration.events.OrdersFetchedEvent
import java.time.Instant
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660085256087
*/
@Component
class ListOfOrdersReadModelProjector(
        private val repository:
                ListOfOrdersReadModelRepository // Using private val for best practice
) {

  @EventHandler
  @Transactional
  fun on(event: OrdersFetchedEvent, @Timestamp axonTimestamp: Instant) {

    // 1. Find existing record by companyId or create a new shell
    val entity =
            repository.findById(event.companyId).orElseGet {
              ListOfOrdersReadModelEntity().apply { this.companyId = event.companyId }
            }

    // 2. Update the state (Replace the JSON list)
    entity.apply {
      this.clientId = event.clientId
      this.orderList = event.orderList // List<OrderDetails> mapped to JSONB
      this.timestamp = axonTimestamp.toEpochMilli()
    }

    // 3. Persist (Save updates the single row for this companyId)
    repository.save(entity)
  }
}
