package administration.companylistlookup

import administration.common.ListOfCompaniesItem
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822859
*/
@Entity
@Table(name = "company_list_lookup")
class CompanyListLookUpReadModelEntity(
        @Id @Column(name = "settingsId", nullable = false) var settingsId: UUID, // Non-nullable
        @Column(name = "connectionId") var connectionId: UUID? = null,
        @Column(name = "timestamp", nullable = false) var timestamp: Long = 0,
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(name = "companies", columnDefinition = "jsonb")
        @JsonProperty("listOfCompanies")
        var listOfCompanies: List<ListOfCompaniesItem>? = null
) : Serializable {
        // Secondary constructor for Hibernate proxy creation
        constructor() : this(UUID.randomUUID(), null, 0, null)
}

data class CompanyListLookUpReadModelQuery(val settingsId: UUID)
