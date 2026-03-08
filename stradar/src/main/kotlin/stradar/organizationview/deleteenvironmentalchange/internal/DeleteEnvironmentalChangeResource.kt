package stradar.organizationview.deleteradarelement.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.deleteenvironmentalchange.DeleteEnvironmentalChangeCommand
import stradar.support.metadata.*

data class DeleteEnvironmentalChangePayload(
        var environmentalChangeId: UUID,
        var teamId: UUID,
        var organizationId: UUID,
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661052478597
*/
@RestController
class DeleteEnvironmentalChangeResource(private var commandGateway: CommandGateway) {

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
        @PostMapping("/debug/deleteenvironmentalchange")
        fun processDebugCommand(
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestParam environmentalChangeId: UUID,
                @RequestParam teamId: UUID,
                @RequestParam organizationId: UUID
        ): CompletableFuture<Any> {
                val metadata =
                        MetaData.with(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and(ORGANIZATION_ID_HEADER, organizationId)
                return commandGateway.send(
                        DeleteEnvironmentalChangeCommand(
                                environmentalChangeId,
                                teamId,
                                organizationId
                        ),
                        metadata
                )
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
        @PostMapping("/deleteenvironmentalchange/{id}")
        fun processCommand(
                @RequestHeader(USER_ID_HEADER) userId: String,
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @PathVariable("id") environmentalChangeId: UUID,
                @RequestBody payload: DeleteEnvironmentalChangePayload
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
                        DeleteEnvironmentalChangeCommand(
                                environmentalChangeId = environmentalChangeId,
                                teamId = payload.teamId,
                                organizationId = payload.organizationId,
                        ),
                        metadata
                )
        }
}
