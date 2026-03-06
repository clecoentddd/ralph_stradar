package stradar.organizationview.environmentalchanges.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.organizationview.environmentalchanges.*

@Component
class EnvironmentalChangesQueryHandler(
        private val repository: EnvironmentalChangesReadModelRepository
) {

        private val logger = KotlinLogging.logger {}

        /** 1. Handles the detail view: scoped by both environmentalChangeId and organizationId */
        @QueryHandler
        fun handle(
                query: EnvironmentalChangesReadModelQuery,
                metaData: MetaData
        ): EnvironmentalChangesReadModel {
                val organizationId = metaData.resolveOrganizationId()

                logger.info {
                        "Fetching elements for id: ${query.environmentalChangeId} (org: $organizationId)"
                }

                val entities =
                        repository.findByEnvironmentalChangeIdAndOrganizationId(
                                query.environmentalChangeId,
                                organizationId
                        )

                if (entities.isEmpty()) {
                        return EnvironmentalChangesReadModel(
                                environmentalChangeId = query.environmentalChangeId,
                                teamId = UUID.randomUUID(),
                                organizationId = organizationId,
                                elements = emptyList()
                        )
                }

                val header = entities.first()

                return EnvironmentalChangesReadModel(
                        environmentalChangeId = query.environmentalChangeId,
                        teamId = header.teamId!!,
                        organizationId = organizationId,
                        elements =
                                entities.map { entity ->
                                        EnvironmentalChangeElementDTO(
                                                environmentalChangeId =
                                                        entity.environmentalChangeId!!,
                                                title = entity.title,
                                                detect = entity.detect,
                                                assess = entity.assess,
                                                respond = entity.respond,
                                                type = entity.type,
                                                category = entity.category,
                                                distance = entity.distance,
                                                impact = entity.impact,
                                                risk = entity.risk
                                        )
                                }
                )
        }

        /** 2. Handles the team list view: scoped by teamId and organizationId */
        @QueryHandler
        fun handle(
                query: EnvironmentalChangesTeamListQuery,
                metaData: MetaData
        ): List<EnvironmentalChangesReadModelEntity> {
                val organizationId = metaData.resolveOrganizationId()

                logger.info {
                        "Fetching flat list for Team: ${query.teamId} (org: $organizationId)"
                }

                return repository.findByTeamIdAndOrganizationId(query.teamId, organizationId)
        }
}
