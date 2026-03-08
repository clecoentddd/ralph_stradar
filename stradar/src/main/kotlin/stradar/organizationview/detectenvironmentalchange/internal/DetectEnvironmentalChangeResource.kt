package stradar.organizationview.detectenvironmentalchange.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.common.*
import stradar.organizationview.domain.commands.detectenvironmentalchange.DetectEnvironmentalChangeCommand
import stradar.support.metadata.*

data class DetectEnvironmentalChangePayload(
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
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661044303442
*/
@RestController
class DetectEnvironmentalChangeResource(private var commandGateway: CommandGateway) {

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
        @PostMapping("/debug/detectenvironmentalchange")
        fun processDebugCommand(
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestParam environmentalChangeId: UUID,
                @RequestParam teamId: UUID,
                @RequestParam organizationId: UUID,
                @RequestParam assess: String,
                @RequestParam category: ChangeCategory,
                @RequestParam detect: String,
                @RequestParam distance: ChangeDistance,
                @RequestParam impact: ChangeImpact,
                @RequestParam respond: String,
                @RequestParam risk: ChangeRisk,
                @RequestParam title: String,
                @RequestParam type: ChangeType,
        ): CompletableFuture<Any> {
                val metadata =
                        MetaData.with(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and(ORGANIZATION_ID_HEADER, organizationId)

                return commandGateway.send(
                        DetectEnvironmentalChangeCommand(
                                environmentalChangeId,
                                teamId,
                                organizationId,
                                assess,
                                category,
                                detect,
                                distance,
                                impact,
                                respond,
                                risk,
                                title,
                                type
                        ),
                        metadata
                )
        }

        @CrossOrigin
        @PostMapping("/detectenvironmentalchange")
        fun processCommand(
                @RequestHeader(USER_ID_HEADER) userId: String,
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestBody payload: DetectEnvironmentalChangePayload
        ): CompletableFuture<Any> {
                val metadata =
                        MetaData.with(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and(USER_ID_HEADER, userId)
                                .and(ORGANIZATION_ID_HEADER, payload.organizationId)

                return commandGateway.send(
                        DetectEnvironmentalChangeCommand(
                                environmentalChangeId = payload.environmentalChangeId,
                                teamId = payload.teamId,
                                assess = payload.assess,
                                category = payload.category,
                                detect = payload.detect,
                                distance = payload.distance,
                                impact = payload.impact,
                                organizationId = payload.organizationId,
                                respond = payload.respond,
                                risk = payload.risk,
                                title = payload.title,
                                type = payload.type
                        ),
                        metadata
                )
        }
}
