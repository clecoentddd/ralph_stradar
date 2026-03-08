package stradar.organizationview.teamlist.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.GenericQueryMessage
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import stradar.organizationview.teamlist.*
import stradar.support.metadata.*

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645849750300
*/
@RestController
class TeamlistResource(private var queryGateway: QueryGateway) {

        private var logger = KotlinLogging.logger {}

        /**
         * Fetches all teams for the organization. The organizationId is now sourced from a
         * mandatory header for security.
         */
        @CrossOrigin(
                allowedHeaders =
                        [
                                ORGANIZATION_ID_HEADER,
                                SESSION_ID_HEADER,
                                "Content-Type",
                                "X-Correlation-Id",
                                USER_ID_HEADER]
        )
        @GetMapping("/teamlist")
        fun findByOrganization(
                @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
                @RequestHeader(USER_ID_HEADER) userId: String
        ): CompletableFuture<TeamListReadModel> {
                logger.info { "Fetching team list for org: $organizationId" }

                val responseType = ResponseTypes.instanceOf(TeamListReadModel::class.java)
                val queryMessage =
                        GenericQueryMessage(
                                        TeamListByOrganizationQuery(organizationId),
                                        responseType
                                )
                                .withMetaData(
                                        MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                                .and(USER_ID_HEADER, userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }

        @CrossOrigin(
                allowedHeaders =
                        [
                                ORGANIZATION_ID_HEADER,
                                SESSION_ID_HEADER,
                                "Content-Type",
                                "X-Correlation-Id",
                                USER_ID_HEADER]
        )
        @GetMapping("/teamlist/{teamId}/name")
        fun findTeamName(
                @PathVariable("teamId") teamId: UUID,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
                @RequestHeader(USER_ID_HEADER) userId: String
        ): CompletableFuture<TeamNameResponse> {
                logger.info { "Fetching name for Team ID: $teamId (org: $organizationId)" }

                val responseType = ResponseTypes.instanceOf(TeamNameResponse::class.java)
                val queryMessage =
                        GenericQueryMessage(TeamNameByTeamIdQuery(teamId), responseType)
                                .withMetaData(
                                        MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                                .and(USER_ID_HEADER, userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }
}
