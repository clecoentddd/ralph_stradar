package stradar.platformadministration.superadminaccount

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import stradar.common.NoArg

// Generic query to fetch the whole list
data class SuperAdminAccountReadModelQuery(val someField: String? = null)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645830935933
*/
@NoArg
@Entity
@Table(name = "super_account_list_read_model")
class SuperAdminAccountReadModelEntity {

  @Id
  @Column(name = "admin_account_id")
  var adminAccountId: UUID? = null // 👈 Essential for JPA and for identification

  @Column(name = "organization_use  _name", unique = true) var username: String? = null
}

data class SuperAdminAccountReadModel(val data: List<SuperAdminAccountReadModelEntity>)
