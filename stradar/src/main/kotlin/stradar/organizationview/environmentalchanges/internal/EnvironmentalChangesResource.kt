package stradar.organizationview.environmentalchanges.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.GenericQueryMessage
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import stradar.organizationview.environmentalchanges.*
import stradar.security.SecurityHelper
import stradar.support.metadata.*

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661040894563
*/
@RestController
class EnvironmentalChangesResource(
        private var queryGateway: QueryGateway,
        private val securityHelper: SecurityHelper
) {

    private val logger = KotlinLogging.logger {}

    /** 1. Fetch a specific Environmental Change View (Hoisted DTO) */
    @CrossOrigin(
            allowedHeaders =
                    [
                            "Authorization",
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
            @RequestHeader(USER_ID_HEADER) userId: String,
            authentication: Authentication
    ): CompletableFuture<ResponseEntity<EnvironmentalChangesReadModel>> {
        logger.info {
            "API Request: Fetching View for $environmentalChangeId (org: $organizationId)"
        }

        // 🔒 Verify user belongs to the requested organization
        val user = securityHelper.extractUser(authentication)
        securityHelper.checkOrganization<EnvironmentalChangesReadModel>(user, organizationId)?.let {
            return CompletableFuture.completedFuture(it)
        }

        val responseType = ResponseTypes.instanceOf(EnvironmentalChangesReadModel::class.java)
        val queryMessage =
                GenericQueryMessage(
                                EnvironmentalChangesReadModelQuery(environmentalChangeId),
                                responseType
                        )
                        .withMetaData(
                                MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                        .and(USER_ID_HEADER, userId)
                        )

        return queryGateway.query(queryMessage, responseType).thenApply { ResponseEntity.ok(it) }
    }

    /** 2. Fetch all Environmental Changes for a Team (Flat List) */
    @CrossOrigin(
            allowedHeaders =
                    [
                            "Authorization",
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
            @RequestHeader(USER_ID_HEADER) userId: String,
            authentication: Authentication
    ): CompletableFuture<ResponseEntity<EnvironmentalChangesReadModel>> {
        logger.info {
            "API Request: Fetching Environmental Changes for team $teamId (org: $organizationId)"
        }

        // 🔒 Verify user belongs to the requested organization
        val user = securityHelper.extractUser(authentication)
        securityHelper.checkOrganization<EnvironmentalChangesReadModel>(user, organizationId)?.let {
            return CompletableFuture.completedFuture(it)
        }

        val responseType = ResponseTypes.instanceOf(EnvironmentalChangesReadModel::class.java)
        val queryMessage =
                GenericQueryMessage(EnvironmentalChangesTeamListQuery(teamId), responseType)
                        .withMetaData(
                                MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                        .and(USER_ID_HEADER, userId)
                        )

        return queryGateway.query(queryMessage, responseType).thenApply { ResponseEntity.ok(it) }
    }
}
