package administration.admin.adminconnected

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.UUID

data class AdminConnectedReadModelQuery(val connectionId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822173
*/
@Entity
class AdminConnectedReadModel {
  @Id @Column(name = "connectionId") var connectionId: UUID? = null
  @Column(name = "email") var email: String? = null
}
