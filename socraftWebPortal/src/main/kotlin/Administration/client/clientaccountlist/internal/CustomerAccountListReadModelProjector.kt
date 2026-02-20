package administration.client.clientaccountlist.internal

import administration.client.clientaccountlist.ClientAccountListReadModelEntity
import administration.events.AccountCreatedEvent
import java.util.UUID
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface ClientAccountListReadModelRepository :
    JpaRepository<ClientAccountListReadModelEntity, UUID> {

  // Added for uniqueness check via QueryHandler
  fun findByClientEmail(clientEmail: String): ClientAccountListReadModelEntity?

  // Added for "fail-fast" existence checks
  fun existsByClientEmail(clientEmail: String): Boolean
}

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660036569910
*/
@Component
class ClientAccountListReadModelProjector(var repository: ClientAccountListReadModelRepository) {

  @EventHandler
  fun on(event: AccountCreatedEvent) {
    // throws exception if not available (adjust logic)
    val entity = this.repository.findById(event.clientId).orElse(ClientAccountListReadModelEntity())
    entity
        .apply {
          clientEmail = event.clientEmail
          clientId = event.clientId
          companyId = event.companyId
          connectionId = event.connectionId
        }
        .also { this.repository.save(it) }
  }
}
