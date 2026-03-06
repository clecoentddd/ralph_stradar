package stradar.organizationview.linkenvironmentalchangestoinitiative

import java.util.UUID
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId

@Component
class InitiativeChangesLinkQueryHandler(private val repository: InitiativeChangesLinkRepository) {

        @QueryHandler
        fun handle(query: GetEnvironmentalLinksByInitiativeQuery, metaData: MetaData): List<UUID> {
                // Strict Security: Extract orgId from MetaData
                val organizationId = metaData.resolveOrganizationId()

                // Fetch from our CRUD Read Model
                return repository.findAllByInitiativeIdAndOrganizationId(
                                query.initiativeId,
                                organizationId
                        )
                        .map { it.envChangeId }
        }
}
