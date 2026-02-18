package administration.client.clientaccountlist

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.UUID

class ClientAccountListReadModelQuery()

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660036569910
*/
@Entity
class ClientAccountListReadModelEntity {
    @Id @Column(name = "clientId") var clientId: UUID? = null
    @Column(name = "clientEmail") var clientEmail: String? = null
    @Column(name = "companyId") var companyId: Long? = null
    @Column(name = "connectionId") var connectionId: UUID? = null
}

data class ClientAccountListReadModel(val data: List<ClientAccountListReadModelEntity>)
