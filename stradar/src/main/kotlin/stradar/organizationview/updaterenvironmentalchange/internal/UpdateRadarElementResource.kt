package stradar.organizationview.updateradarelement.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.common.*
import stradar.organizationview.domain.commands.updateenvironmentalchange.UpdateEnvironmentalChangeCommand
import stradar.support.metadata.*

data class UpdateEnvironmentalChangePayload(
        var environmentalChangeId: UUID,
        var teamId: UUID,
        var organizationId: UUID,
        var assess: String,
        var category: ChangeCategory,
        var detect: String,
        var distance: ChangeDistance,
        var impact: ChangeImpact,
        var respond: String,
        var risk: ChangeRisk,
        var title: String,
        var type: ChangeType
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661051289511
*/
@RestController
class UpdateEnvironmentalChangeResource(private var commandGateway: CommandGateway) {

        var logger = KotlinLogging.logger {}

        @CrossOrigin(
                allowedHeaders =
                        [
                                ORGANIZATION_ID_HEADER,
                                SESSION_ID_HEADER,
                                "Content-Type",
                                "X-Correlation-Id",
                                USER_ID_HEADER]
        )
        @PostMapping("/updateenvironmentalchange/{id}")
        fun processCommand(
                @RequestHeader(USER_ID_HEADER) userId: String,
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @PathVariable("id") environmentalChangeId: UUID, // This maps to the Aggregate
                @RequestBody payload: UpdateEnvironmentalChangePayload
        ): CompletableFuture<Any> {

                logger.info {
                        "Updating Element ${payload.environmentalChangeId} inside Radar $environmentalChangeId"
                }

                val metadata =
                        MetaData.with(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and(USER_ID_HEADER, userId)
                                .and(ORGANIZATION_ID_HEADER, payload.organizationId)

                return commandGateway.send(
                        UpdateEnvironmentalChangeCommand(
                                environmentalChangeId =
                                        environmentalChangeId, // TargetAggregateIdentifier
                                teamId = payload.teamId,
                                organizationId = payload.organizationId,
                                assess = payload.assess,
                                category = payload.category,
                                detect = payload.detect,
                                distance = payload.distance,
                                impact = payload.impact,
                                respond = payload.respond,
                                risk = payload.risk,
                                title = payload.title,
                                type = payload.type
                        ),
                        metadata
                )
        }
}
