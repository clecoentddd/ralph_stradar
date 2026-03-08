package stradar.organizationview.teamlist.internal

import mu.KotlinLogging
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.organizationview.teamlist.TeamListByOrganizationQuery
import stradar.organizationview.teamlist.TeamListReadModel
import stradar.organizationview.teamlist.TeamNameAlreadyExistsQuery
import stradar.organizationview.teamlist.TeamNameByTeamIdQuery
import stradar.organizationview.teamlist.TeamNameResponse

@Component
class TeamListReadModelQueryHandler(private val repository: TeamListReadModelRepository) {

        private val logger = KotlinLogging.logger {}

        /**
         * Checks if a team name is already taken within the organization. Returns true if the name
         * is UNIQUE (available), false if it exists.
         */
        @QueryHandler
        fun handle(query: TeamNameAlreadyExistsQuery, metadata: MetaData): Boolean {
                // Enforce security by resolving OrgId from MetaData
                val organizationId = metadata.resolveOrganizationId()

                // Check the database. If it exists, it is NOT unique.
                val exists =
                        repository.existsByNameAndOrganizationId(query.teamName, organizationId)

                logger.info { "Team name ${query.teamName} exists: $exists" }
                return exists
        }

        /**
         * Fetches all teams scoped to the organization. Enforces organizationId from MetaData to
         * prevent cross-organization data leakage.
         */
        @QueryHandler
        fun handleByOrganization(
                query: TeamListByOrganizationQuery,
                metadata: MetaData
        ): TeamListReadModel {
                val organizationId = metadata.resolveOrganizationId()

                return TeamListReadModel(repository.findByOrganizationIdAndStatus(organizationId))
        }

        /**
         * Fetches the name of a specific team. Also enforces an organization matching check to
         * ensure the team belongs to the requester's org.
         */
        @QueryHandler
        fun handle(query: TeamNameByTeamIdQuery, metadata: MetaData): TeamNameResponse {
                val organizationId = metadata.resolveOrganizationId()

                val team =
                        repository.findById(query.teamId).orElseThrow {
                                IllegalStateException("Team with ID ${query.teamId} not found")
                        }

                if (team.organizationId != organizationId) {
                        throw IllegalStateException(
                                "Access denied: team ${query.teamId} does not belong to org $organizationId"
                        )
                }

                return TeamNameResponse(teamName = team.name ?: "Unknown Team")
        }
}
