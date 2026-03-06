package stradar.organizationview.strategies.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.GenericQueryMessage
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import stradar.organizationview.strategies.GetStrategiesByOrganizationQuery
import stradar.organizationview.strategies.StrategiesReadModel

/*
Boardlink:
https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661684920684
*/

@RestController
@RequestMapping("/strategies")
class StrategiesResource(private val queryGateway: QueryGateway) {

        private val logger = KotlinLogging.logger {}

        /**
         * GET /strategies
         *
         * organizationId is taken from a trusted request header, never from a query parameter.
         */
        @CrossOrigin(
                allowedHeaders =
                        [
                                "organizationId",
                                "X-Session-Id",
                                "X-Correlation-Id",
                                "Content-Type",
                                "x-user-id"]
        )
        @GetMapping
        fun findStrategies(
                @RequestHeader("organizationId") organizationId: UUID,
                @RequestHeader("x-user-id") userId: String
        ): CompletableFuture<StrategiesReadModel> {
                logger.info { "Querying strategies for org: $organizationId" }

                val responseType = ResponseTypes.instanceOf(StrategiesReadModel::class.java)
                val queryMessage =
                        GenericQueryMessage(
                                        GetStrategiesByOrganizationQuery(organizationId),
                                        responseType
                                )
                                .withMetaData(
                                        MetaData.with("organizationId", organizationId)
                                                .and("x-user-id", userId)
                                )

                return queryGateway.query(queryMessage, responseType)
        }
}
