package administration.client.projectlistlookup

import administration.common.ProjectDetails
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

data class ListOfProjectsReadModelQuery(val companyId: Long)

@Repository
interface ListOfProjectsReadModelRepository : JpaRepository<ListOfProjectsReadModelEntity, Long>

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660065978340
*/
@Entity
@Table(name = "project_list_lookup")
class ListOfProjectsReadModelEntity : Serializable {

  @Id @Column(name = "companyId", nullable = false) var companyId: Long? = null

  @Column(name = "companyName") var companyName: String? = null

  @Column(name = "clientId") var clientId: UUID? = null

  @Column(name = "timestamp") var timestamp: Long? = null

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "project_list", columnDefinition = "jsonb")
  @JsonProperty("projectList")
  // Now that @Embeddable is removed from ProjectDetails,
  // we can use the specific type safely.
  var projectList: List<ProjectDetails> = mutableListOf()
}
