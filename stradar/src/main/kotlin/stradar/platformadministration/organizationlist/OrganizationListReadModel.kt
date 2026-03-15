package stradar.platformadministration.organizationlist

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import stradar.common.NoArg

data class OrganizationListReadModelQuery(val someField: String? = null) // Generic for list

data class OrganizationNameQuery(val organizationName: String) // Centralized for uniqueness

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645830935933
*/
@NoArg
@Entity
@Table(name = "organization_list_read_model")
class OrganizationListReadModelEntity {
  @Id @Column(name = "organizationId") var organizationId: UUID? = null
  @Column(name = "organizationName") var organizationName: String? = null
  @Column(name = "organizationUserId") var organizationUserId: UUID? = null
  @Column(name = "organizationUserEmail") var organizationUserEmail: String? = null
  @Column(name = "role") var role: String? = null
}

// Define the specific query for uniqueness check
data class OrganizationNameUniqueQuery(val organizationName: String)

data class OrganizationListReadModel(val data: List<OrganizationListReadModelEntity>)
