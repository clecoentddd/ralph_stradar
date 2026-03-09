package stradar.organizationview.strategies

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import stradar.common.StrategyStatus

/*
Boardlink:
https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661684920684
*/

@Entity
@Table(name = "strategies_view")
class StrategiesReadModelEntity {

    @Id @Column(name = "strategyId") var strategyId: UUID? = null

    @Column(name = "strategyBuilderId") var strategyBuilderId: String? = null

    @Column(name = "organizationId", nullable = false) var organizationId: UUID? = null

    @Column(name = "teamId", nullable = false) var teamId: UUID? = null

    @Column(name = "strategyName") var strategyName: String? = null

    @Column(name = "strategyTimeframe") var strategyTimeframe: String? = null

    @Enumerated(EnumType.STRING) @Column(name = "status") var status: StrategyStatus? = null
}

data class StrategiesReadModel(val strategies: List<StrategiesReadModelEntity>)

data class GetStrategiesByOrganizationQuery(val organizationId: UUID)

data class GetStrategiesByTeamQuery(val teamId: UUID)
