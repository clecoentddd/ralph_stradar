package stradar.organizationview.teamlist

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import stradar.common.NoArg

class TeamListReadModelQuery()

data class TeamListByOrganizationQuery(val organizationId: java.util.UUID)

data class TeamNameAlreadyExistsQuery(val organizationId: java.util.UUID, val teamName: String)

/** STEP 1: Add the query to fetch Name by Team ID */
data class TeamNameByTeamIdQuery(val teamId: UUID)

/** STEP 2: Add the response DTO */
data class TeamNameResponse(val teamName: String)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645849750300
*/
@NoArg
@Entity
@Table(name = "team_read_model")
class TeamListReadModelEntity {
  @Id @Column(name = "team_id") var teamId: UUID? = null
  @Column(name = "context", columnDefinition = "TEXT") var context: String? = null
  @Column(name = "level") var level: Int? = null
  @Column(name = "name") var name: String? = null
  @Column(name = "organization_id") var organizationId: UUID? = null
  @Column(name = "organization_name") var organizationName: String? = null
  @Column(name = "admin_account_id") var adminAccountId: UUID? = null
  @Column(name = "purpose", columnDefinition = "TEXT") var purpose: String? = null
  @Column(name = "status") var status: String? = "ACTIVE"
  @Column(name = "reason") var reason: String? = null
}

data class TeamListReadModel(val teams: List<TeamListReadModelEntity>)
