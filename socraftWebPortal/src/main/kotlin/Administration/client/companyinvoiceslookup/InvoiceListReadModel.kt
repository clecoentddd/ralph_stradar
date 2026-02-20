package administration.client.companyinvoiceslookup

import administration.common.InvoiceDetails
import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

data class InvoiceListReadModelQuery(val companyId: Long)

@Repository
interface InvoiceListReadModelRepository : JpaRepository<InvoiceListReadModelEntity, Long>

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660086962398
*/
@Entity
@Table(name = "company_invoices_lookup")
class InvoiceListReadModelEntity : Serializable {

  @Id @Column(name = "companyId", nullable = false) var companyId: Long? = null

  @Column(name = "clientId") var clientId: UUID? = null

  @Column(name = "timestamp") var timestamp: Long? = null

  // Pattern aligned with Orders/Projects: Use JSONB instead of @ElementCollection
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "invoice_list", columnDefinition = "jsonb")
  var invoiceList: List<InvoiceDetails> = mutableListOf()
}

data class InvoiceListReadModel(val data: InvoiceListReadModelEntity)
