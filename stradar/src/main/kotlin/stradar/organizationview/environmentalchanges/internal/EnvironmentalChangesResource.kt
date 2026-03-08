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
import stradar.support.metadata.*

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
                                ORGANIZATION_ID_HEADER,
                                SESSION_ID_HEADER,
                                "X-Correlation-Id",
                                "Content-Type",
                                USER_ID_HEADER]
        )
        @GetMapping("/environmentalchanges/{environmentalChangeId}")
        fun findReadModel(
                @PathVariable("environmentalChangeId") environmentalChangeId: UUID,
                @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
                @RequestHeader(USER_ID_HEADER) userId: String
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
                                        MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                                .and(USER_ID_HEADER, userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }

        /** 2. Fetch all Environmental Changes for a Team (Flat List) */
        @CrossOrigin(
                allowedHeaders =
                        [
                                ORGANIZATION_ID_HEADER,
                                SESSION_ID_HEADER,
                                "X-Correlation-Id",
                                "Content-Type",
                                USER_ID_HEADER]
        )
        @GetMapping("/environmentalchanges/team/{teamId}")
        fun findByTeam(
                @PathVariable("teamId") teamId: UUID,
                @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
                @RequestHeader(USER_ID_HEADER) userId: String
        ): CompletableFuture<EnvironmentalChangesReadModel> { // 👈 Change return type from List

                val responseType =
                        ResponseTypes.instanceOf(
                                EnvironmentalChangesReadModel::class.java
                        ) // 👈 Use instanceOf

                val queryMessage =
                        GenericQueryMessage(EnvironmentalChangesTeamListQuery(teamId), responseType)
                                .withMetaData(
                                        MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                                .and(USER_ID_HEADER, userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }
}
