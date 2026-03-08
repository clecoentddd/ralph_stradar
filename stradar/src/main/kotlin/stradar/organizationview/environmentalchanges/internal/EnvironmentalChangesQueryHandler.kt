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
                        "QUERY: Fetching Details for Change [${query.environmentalChangeId}] (Org: $organizationId)"
                }

                val entities =
                        repository.findByEnvironmentalChangeIdAndOrganizationId(
                                query.environmentalChangeId,
                                organizationId
                        )

                logger.info {
                        "RESULT: Found ${entities.size} rows for Change ID [${query.environmentalChangeId}]"
                }

                return EnvironmentalChangesReadModel(
                        environmentalChangeId = query.environmentalChangeId,
                        teamId = entities.firstOrNull()?.teamId ?: UUID.randomUUID(),
                        organizationId = organizationId,
                        elements = entities.map { it.toDTO() }
                )
        }

        /** 2. Handles the team list view: Returns the wrapper DTO with the list of elements */
        @QueryHandler
        fun handle(
                query: EnvironmentalChangesTeamListQuery,
                metaData: MetaData
        ): EnvironmentalChangesReadModel {
                val organizationId = metaData.resolveOrganizationId()

                logger.info {
                        "QUERY: Fetching Team List for Team [${query.teamId}] (Org: $organizationId)"
                }

                val entities =
                        repository.findByTeamIdAndOrganizationId(query.teamId, organizationId)

                // CRITICAL LOG: This tells us if the database query returned 0 rows
                logger.info {
                        "RESULT: Found ${entities.size} elements for Team [${query.teamId}] in Org [$organizationId]"
                }

                return EnvironmentalChangesReadModel(
                        environmentalChangeId = entities.firstOrNull()?.environmentalChangeId
                                        ?: UUID.randomUUID(),
                        teamId = query.teamId,
                        organizationId = organizationId,
                        elements = entities.map { it.toDTO() }
                )
        }

        private fun EnvironmentalChangesReadModelEntity.toDTO() =
                EnvironmentalChangeElementDTO(
                        environmentalChangeId = this.environmentalChangeId!!,
                        title = this.title,
                        detect = this.detect,
                        assess = this.assess,
                        respond = this.respond,
                        type = this.type,
                        category = this.category,
                        distance = this.distance,
                        impact = this.impact,
                        risk = this.risk
                )
}
