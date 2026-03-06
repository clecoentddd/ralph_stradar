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
import org.springframework.web.bind.annotation.*
import stradar.organizationview.initiativelist.InitiativeListResponse
import stradar.organizationview.initiativelist.InitiativesByStrategyQuery
import stradar.organizationview.initiativelist.InitiativesReadModel
import stradar.organizationview.initiativelist.InitiativesReadModelQuery

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645855652122
*/
@RestController
@Tag(name = "Initiative Queries", description = "Endpoints for retrieving initiative read models")
class InitiativelistResource(private val queryGateway: QueryGateway) {

        private val logger = KotlinLogging.logger {}

        /** Finds a specific initiative by its unique ID */
        @CrossOrigin(
                allowedHeaders =
                        [
                                "organizationId",
                                "X-Session-Id",
                                "X-Correlation-Id",
                                "Content-Type",
                                "x-user-id"]
        )
        @GetMapping("/initiativelist/{id}")
        fun findReadModel(
                @Parameter(
                        description = "The UUID of the initiative",
                        example = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
                )
                @PathVariable("id")
                initiativeId: UUID,
                @RequestHeader("organizationId") organizationId: UUID,
                @RequestHeader("x-user-id") userId: String
        ): CompletableFuture<InitiativesReadModel> {
                logger.debug { "Querying single initiative: $initiativeId (org: $organizationId)" }

                val responseType = ResponseTypes.instanceOf(InitiativesReadModel::class.java)
                val queryMessage =
                        GenericQueryMessage(InitiativesReadModelQuery(initiativeId), responseType)
                                .withMetaData(
                                        MetaData.with("organizationId", organizationId)
                                                .and("x-user-id", userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }

        /** Coherent query: Finds all initiatives for a specific strategy, team, and organization */
        @Operation(summary = "Get initiatives for a specific strategy context")
        @CrossOrigin(
                allowedHeaders =
                        [
                                "organizationId",
                                "X-Session-Id",
                                "X-Correlation-Id",
                                "Content-Type",
                                "x-user-id"]
        )
        @GetMapping("/initiativelist/by-strategy")
        fun findByStrategy(
                @RequestParam
                @Parameter(example = "77777777-7777-7777-7777-777777777777")
                strategyId: UUID,
                @RequestParam
                @Parameter(example = "18ed5446-4fc6-4dd5-8e98-5b9c5cbf130d")
                teamId: UUID,
                @RequestHeader("organizationId") organizationId: UUID,
                @RequestHeader("x-user-id") userId: String
        ): CompletableFuture<InitiativeListResponse> {
                logger.info {
                        "Querying initiatives for strategy $strategyId, team $teamId (org: $organizationId)"
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
                                        MetaData.with("organizationId", organizationId)
                                                .and("x-user-id", userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }
}
