package administration.admin.invoicestatemappinglookup

import administration.common.ListOfInvoiceStatesItem
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

data class InvoiceStateMappingLookUpReadModelQuery(val settingsId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713541
*/
@Entity
@Table(name = "invoice_state_mapping_lookup")
class InvoiceStateMappingLookUpReadModelEntity {

   @Id @Column(name = "settingsId") var settingsId: UUID? = null

   @Column(name = "connectionId") var connectionId: UUID? = null

   @Column(name = "timestamp") // New column
   var timestamp: Long? = null

   @JdbcTypeCode(SqlTypes.JSON)
   @Column(name = "invoice_states", columnDefinition = "jsonb")
   var listOfInvoiceStates: List<ListOfInvoiceStatesItem> = mutableListOf()
}

data class InvoiceStateMappingLookUpReadModel(val data: InvoiceStateMappingLookUpReadModelEntity)
