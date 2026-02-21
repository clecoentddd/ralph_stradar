package administration.admin.systemstatus.internal

import administration.admin.systemstatus.SystemStatusReadModelEntity
import administration.events.SettingsCreatedEvent
import java.util.UUID
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface SystemStatusReadModelRepository : JpaRepository<SystemStatusReadModelEntity, UUID>

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659756306810
*/
@Component
class SystemStatusReadModelProjector(var repository: SystemStatusReadModelRepository) {

    @EventHandler
    fun on(event: SettingsCreatedEvent) {
        // throws exception if not available (adjust logic)
        val entity =
                this.repository.findById(event.settingsId).orElse(SystemStatusReadModelEntity())
        entity.apply { settingsId = event.settingsId }.also { this.repository.save(it) }
    }
}
