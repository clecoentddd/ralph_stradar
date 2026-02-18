package administration.adminconnected.internal

import administration.adminconnected.AdminConnectedReadModelEntity
import administration.events.AdminConnectedEvent
import java.util.UUID
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface AdminConnectedReadModelRepository : JpaRepository<AdminConnectedReadModelEntity, UUID>

/*
        // AI-TODO:

# Spec Start
Title: spec: Admin Connected - Checking connectionId is not null
### Given (Events):
  * 'Admin Connected' (SPEC_EVENT)
Fields:
 - connectionId: 24af641b-d7ef-43ce-8325-79089244a4a8
 - email: test@event.com
### When (Command): None
### Then:
  * 'Admin Connected' (SPEC_READMODEL)
Fields:
 - connectionId: 24af641b-d7ef-43ce-8325-79089244a4a8
 - email: test@event.com
# Spec End */
/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822173
*/
@Component
class AdminConnectedReadModelProjector(var repository: AdminConnectedReadModelRepository) {

  @EventHandler
  fun on(event: AdminConnectedEvent) {
    // throws exception if not available (adjust logic)
    val entity =
        this.repository.findById(event.connectionId).orElse(AdminConnectedReadModelEntity())
    entity
        .apply {
          connectionId = event.connectionId
          email = event.email
        }
        .also { this.repository.save(it) }
  }
}
