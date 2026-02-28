package stradar.organizationview.accountlist

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import stradar.common.NoArg

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645830935933
*/

/** 1. The Request: "Give me the list" */
class AccountListReadModelQuery

/** 2. The Result: Wrapper for the list of data */
data class AccountListReadModel(val data: List<AccountListReadModelEntity>)

/** 3. The Specific Request: "Give me this person's context" */
data class PersonAccountQuery(val personId: UUID)

/** 4. The Storage Entity */
@NoArg
@Entity
@Table(name = "person_list_read_model")
class AccountListReadModelEntity {
  @Id @Column(name = "personId") var personId: UUID? = null
  @Column(name = "organizationId") var organizationId: UUID? = null
  @Column(name = "organizationName") var organizationName: String? = null
  @Column(name = "role") var role: String? = null
  @Column(name = "username") var username: String? = null
}
