package stradar.organizationview.environmentalchanges.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.GenericQueryMessage
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import stradar.organizationview.environmentalchanges.*

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661040894563
*/
@RestController
class EnvironmentalChangesResource(private var queryGateway: QueryGateway) {

        private val logger = KotlinLogging.logger {}

        /** 1. Fetch a specific Environmental Change View (Hoisted DTO) */
        @CrossOrigin(
                allowedHeaders =
                        [
                                "organizationId",
                                "X-Session-Id",
                                "X-Correlation-Id",
                                "Content-Type",
                                "x-user-id"]
        )
        @GetMapping("/environmentalchanges/{environmentalChangeId}")
        fun findReadModel(
                @PathVariable("environmentalChangeId") environmentalChangeId: UUID,
                @RequestHeader("organizationId") organizationId: UUID,
                @RequestHeader("x-user-id") userId: String
        ): CompletableFuture<EnvironmentalChangesReadModel> {
                logger.info {
                        "API Request: Fetching View for $environmentalChangeId (org: $organizationId)"
                }

                val responseType =
                        ResponseTypes.instanceOf(EnvironmentalChangesReadModel::class.java)
                val queryMessage =
                        GenericQueryMessage(
                                        EnvironmentalChangesReadModelQuery(environmentalChangeId),
                                        responseType
                                )
                                .withMetaData(
                                        MetaData.with("organizationId", organizationId)
                                                .and("x-user-id", userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }

        /** 2. Fetch all Environmental Changes for a Team (Flat List) */
        @CrossOrigin(
                allowedHeaders =
                        [
                                "organizationId",
                                "X-Session-Id",
                                "X-Correlation-Id",
                                "Content-Type",
                                "x-user-id"]
        )
        @GetMapping("/environmentalchanges/team/{teamId}")
        fun findByTeam(
                @PathVariable("teamId") teamId: UUID,
                @RequestHeader("organizationId") organizationId: UUID,
                @RequestHeader("x-user-id") userId: String
        ): CompletableFuture<List<EnvironmentalChangesReadModelEntity>> {
                logger.info {
                        "API Request: Fetching all changes for Team: $teamId (org: $organizationId)"
                }

                val responseType =
                        ResponseTypes.multipleInstancesOf(
                                EnvironmentalChangesReadModelEntity::class.java
                        )
                val queryMessage =
                        GenericQueryMessage(EnvironmentalChangesTeamListQuery(teamId), responseType)
                                .withMetaData(
                                        MetaData.with("organizationId", organizationId)
                                                .and("x-user-id", userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }
}
