package administration.admin.systemstatus

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.UUID

data class SystemStatusReadModelQuery(val settingsId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659756306810
*/
@Entity
class SystemStatusReadModelEntity(
        @Id var settingsId: UUID? = null,
        @Column(nullable = false) var isInitialized: Boolean = false
)

data class SystemStatusReadModel(val settingsId: UUID, val isInitialized: Boolean)
