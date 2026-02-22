package administration.client.clientaccountlist

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

data class ClientAccountListReadModelQuery(
        val email: String? = null,
        val companyId: Long? = null
) // Nullable to allow "Get All"

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660036569910
*/
@Entity
@Table(name = "client_account_list_read_model")
class ClientAccountListReadModel {
  @Id @Column(name = "clientId") var clientId: UUID? = null
  @Column(name = "clientEmail") var clientEmail: String? = null
  @Column(name = "companyId") var companyId: Long? = null
  @Column(name = "connectionId") var connectionId: UUID? = null
}
