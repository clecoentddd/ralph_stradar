package administration.client.companyorderlistlookup

import administration.common.ListOfOrdersItem
import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

data class ListOfOrdersReadModelQuery(val companyId: Long)

@Repository
interface ListOfOrdersReadModelRepository : JpaRepository<ListOfOrdersReadModelEntity, Long>

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660085256087
*/
@Entity
@Table(name = "company_order_list_lookup")
class ListOfOrdersReadModelEntity : Serializable {

   @Id @Column(name = "companyId", nullable = false) var companyId: Long? = null

   @Column(name = "clientId") var clientId: UUID? = null

   @Column(name = "timestamp") var timestamp: Long? = null

   // Pattern aligned with Invoices/Projects: No @ElementCollection
   @JdbcTypeCode(SqlTypes.JSON)
   @Column(name = "order_list", columnDefinition = "jsonb")
   var orderList: List<ListOfOrdersItem> = mutableListOf()
}

data class ListOfOrdersReadModel(val data: ListOfOrdersReadModelEntity)
