package administration.admin.companylistlookup

import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID

@Entity
@Table(name = "company_list_lookup")
class CompanyListLookUpReadModelEntity(
        @Id
        @Column(name = "company_id", nullable = false)
        var companyId: Long, // Now the Primary Key
        @Column(name = "company_name", nullable = false) var companyName: String,
        @Column(name = "settings_id", nullable = false) var settingsId: UUID,
        @Column(name = "connection_id") var connectionId: UUID? = null,
        @Column(name = "timestamp", nullable = false) var timestamp: Long = 0
) : Serializable {
  // Required for Hibernate
  constructor() : this(0L, "", UUID.randomUUID(), null, 0L)
}

/** Used for the Admin "All" view */
class FetchAllCompaniesQuery

/** Used for the Client "Specific Name" view */
data class FetchCompanyNameByCompanyIdQuery(val companyId: Long)
