package stradar.organizationview.strategies.internal

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
import stradar.organizationview.strategies.GetStrategiesByOrganizationQuery
import stradar.organizationview.strategies.GetStrategiesByTeamQuery
import stradar.organizationview.strategies.StrategiesReadModel
import stradar.security.SecurityHelper
import stradar.support.metadata.*

/*
Boardlink:
https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661684920684
*/

@RestController
@RequestMapping("/strategies")
class StrategiesResource(
        private val queryGateway: QueryGateway,
        private val securityHelper: SecurityHelper
) {

    private val logger = KotlinLogging.logger {}

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
    @GetMapping
    fun findStrategies(
            @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
            authentication: Authentication
    ): CompletableFuture<ResponseEntity<StrategiesReadModel>> {
        logger.info { "Querying strategies for org: $organizationId" }

        // 🔒 Verify user belongs to the requested organization
        val user = securityHelper.extractUser(authentication)
        val userId = user.auth0UserId
        securityHelper.checkOrganization<StrategiesReadModel>(user, organizationId)?.let {
            return CompletableFuture.completedFuture(it)
        }

        val responseType = ResponseTypes.instanceOf(StrategiesReadModel::class.java)
        val queryMessage =
                GenericQueryMessage(GetStrategiesByOrganizationQuery(organizationId), responseType)
                        .withMetaData(
                                MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                        .and(USER_ID_HEADER, userId)
                        )

        return queryGateway.query(queryMessage, responseType).thenApply { ResponseEntity.ok(it) }
    }

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
    @GetMapping("/team/{teamId}")
    fun findStrategiesByTeam(
            @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
            @PathVariable teamId: UUID,
            authentication: Authentication
    ): CompletableFuture<ResponseEntity<StrategiesReadModel>> {
        logger.info { "Querying strategies for team: $teamId within org: $organizationId" }

        // 🔒 Verify user belongs to the requested organization
        val user = securityHelper.extractUser(authentication)
        val userId = user.auth0UserId
        securityHelper.checkOrganization<StrategiesReadModel>(user, organizationId)?.let {
            return CompletableFuture.completedFuture(it)
        }

        val responseType = ResponseTypes.instanceOf(StrategiesReadModel::class.java)
        val queryMessage =
                GenericQueryMessage(GetStrategiesByTeamQuery(teamId), responseType)
                        .withMetaData(
                                MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                        .and(USER_ID_HEADER, userId)
                        )

        return queryGateway.query(queryMessage, responseType).thenApply { ResponseEntity.ok(it) }
    }
}
