package stradar.organizationview.initiativelist.internal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
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
import stradar.organizationview.initiativelist.AllInitiativesForOrganizationQuery
import stradar.organizationview.initiativelist.InitiativeListResponse
import stradar.organizationview.initiativelist.InitiativesByStrategyQuery
import stradar.organizationview.initiativelist.InitiativesReadModel
import stradar.organizationview.initiativelist.InitiativesReadModelQuery
import stradar.organizationview.initiativelist.OrganizationInitiativeListResponse
import stradar.security.SecurityHelper
import stradar.support.metadata.*

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645855652122
*/
@RestController
@Tag(name = "Initiative Queries", description = "Endpoints for retrieving initiative read models")
class InitiativelistResource(
        private val queryGateway: QueryGateway,
        private val securityHelper: SecurityHelper
) {

    private val logger = KotlinLogging.logger {}

    /** Finds a specific initiative by its unique ID */
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
    @GetMapping("/initiativelist/{id}")
    fun findReadModel(
            @Parameter(
                    description = "The UUID of the initiative",
                    example = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
            )
            @PathVariable("id")
            initiativeId: UUID,
            @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
            authentication: Authentication
    ): CompletableFuture<ResponseEntity<InitiativesReadModel>> {
        logger.debug { "Querying single initiative: $initiativeId (org: $organizationId)" }

        // 🔒 Verify user belongs to the requested organization
        val user = securityHelper.extractUser(authentication)
        val userId = user.auth0UserId // ← derived from JWT, not header
        securityHelper.checkOrganization<InitiativesReadModel>(user, organizationId)?.let {
            return CompletableFuture.completedFuture(it)
        }

        val responseType = ResponseTypes.instanceOf(InitiativesReadModel::class.java)
        val queryMessage =
                GenericQueryMessage(InitiativesReadModelQuery(initiativeId), responseType)
                        .withMetaData(
                                MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                        .and(USER_ID_HEADER, userId)
                        )

        return queryGateway.query<InitiativesReadModel, Any>(queryMessage, responseType).thenApply {
            ResponseEntity.ok(it)
        }
    }

    /** Coherent query: Finds all initiatives for a specific strategy, team, and organization */
    @Operation(summary = "Get initiatives for a specific strategy context")
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
    @GetMapping("/initiativelist/by-strategy")
    fun findByStrategy(
            @RequestParam
            @Parameter(example = "77777777-7777-7777-7777-777777777777")
            strategyId: UUID,
            @RequestParam @Parameter(example = "18ed5446-4fc6-4dd5-8e98-5b9c5cbf130d") teamId: UUID,
            @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
            authentication: Authentication
    ): CompletableFuture<ResponseEntity<InitiativeListResponse>> {
        logger.info {
            "Querying initiatives for strategy $strategyId, team $teamId (org: $organizationId)"
        }

        // 🔒 Verify user belongs to the requested organization
        val user = securityHelper.extractUser(authentication)
        val userId = user.auth0UserId // ← derived from JWT, not header
        securityHelper.checkOrganization<InitiativeListResponse>(user, organizationId)?.let {
            return CompletableFuture.completedFuture(it)
        }

        val responseType = ResponseTypes.instanceOf(InitiativeListResponse::class.java)
        val queryMessage =
                GenericQueryMessage(
                                InitiativesByStrategyQuery(
                                        strategyId = strategyId,
                                        teamId = teamId,
                                        organizationId = organizationId
                                ),
                                responseType
                        )
                        .withMetaData(
                                MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                        .and(USER_ID_HEADER, userId)
                        )

        return queryGateway.query<InitiativeListResponse, Any>(queryMessage, responseType)
                .thenApply { ResponseEntity.ok(it) }
    }

    /** Finds all initiatives for the entire organization (for cross-strategy linking) */
    @Operation(summary = "Get all initiatives for an organization")
    @CrossOrigin(
            allowedHeaders =
                    [
                            "Authorization",
                            ORGANIZATION_ID_HEADER,
                            SESSION_ID_HEADER,
                            "X-Correlation-Id",
                            "X-Correlation-ID",
                            "Content-Type",
                            USER_ID_HEADER]
    )
    @GetMapping("/initiativelist/by-organization")
    fun findByOrganization(
            @RequestParam(required = false) organizationIdParam: UUID?,
            @RequestHeader(value = ORGANIZATION_ID_HEADER, required = false)
            organizationIdHeader: UUID?,
            authentication: Authentication
    ): CompletableFuture<ResponseEntity<OrganizationInitiativeListResponse>> {
        val organizationId =
                organizationIdParam
                        ?: organizationIdHeader
                                ?: throw IllegalArgumentException(
                                "organizationId is required either as query param or header"
                        )

        logger.info { "Querying all initiatives for organization: $organizationId" }

        // 🔒 Verify user belongs to the requested organization
        val user = securityHelper.extractUser(authentication)
        val userId = user.auth0UserId // ← derived from JWT, not header
        securityHelper.checkOrganization<OrganizationInitiativeListResponse>(user, organizationId)
                ?.let {
                    return CompletableFuture.completedFuture(it)
                }

        val responseType = ResponseTypes.instanceOf(OrganizationInitiativeListResponse::class.java)
        val queryMessage =
                GenericQueryMessage(
                                AllInitiativesForOrganizationQuery(organizationId = organizationId),
                                responseType
                        )
                        .withMetaData(
                                MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                                        .and(USER_ID_HEADER, userId)
                        )

        return queryGateway.query<OrganizationInitiativeListResponse, Any>(
                        queryMessage,
                        responseType
                )
                .thenApply { ResponseEntity.ok(it) }
    }
}
