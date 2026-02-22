package administration.admin.adminconnected.internal

import administration.admin.adminconnected.AdminConnectedReadModel
import administration.events.AdminConnectedEvent
import java.util.UUID
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface AdminConnectedReadModelRepository : JpaRepository<AdminConnectedReadModel, UUID>

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822173
*/
@Component
class AdminConnectedReadModelProjector(var repository: AdminConnectedReadModelRepository) {

  @EventHandler
  fun on(event: AdminConnectedEvent) {
    // throws exception if not available (adjust logic)
    val entity = this.repository.findById(event.connectionId).orElse(AdminConnectedReadModel())
    entity
            .apply {
              connectionId = event.connectionId
              email = event.email
            }
            .also { this.repository.save(it) }
  }
}
