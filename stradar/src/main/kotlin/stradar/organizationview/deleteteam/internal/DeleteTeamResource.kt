package stradar.organizationview.deleteteam.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand
import stradar.support.metadata.SESSION_ID_HEADER

data class DeleteTeamPayload(var teamId: UUID, var organizationId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631612141
*/
@RestController
class DeleteTeamResource(private var commandGateway: CommandGateway) {

        var logger = KotlinLogging.logger {}

        @CrossOrigin(
                allowedHeaders =
                        [
                                "organizationId",
                                SESSION_ID_HEADER,
                                "Content-Type",
                                "X-Correlation-Id",
                                "x-user-id"]
        )
        @PostMapping("/debug/deleteteam")
        fun processDebugCommand(
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestParam teamId: UUID,
                @RequestParam organizationId: UUID
        ): CompletableFuture<Any> {
                val metadata =
                        MetaData.with(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and("organizationId", organizationId)

                return commandGateway.send(DeleteTeamCommand(teamId, organizationId), metadata)
        }

        @CrossOrigin
        @PostMapping("/deleteteam/{id}")
        fun processCommand(
                @RequestHeader("x-user-id") userId: String,
                @RequestHeader(SESSION_ID_HEADER) sessionId: String,
                @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
                @PathVariable("id") teamId: UUID,
                @RequestBody payload: DeleteTeamPayload
        ): CompletableFuture<Any> {
                val metadata =
                        MetaData.with("x-user-id", userId)
                                .and(SESSION_ID_HEADER, sessionId)
                                .and(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and("organizationId", payload.organizationId)

                return commandGateway.send(
                        DeleteTeamCommand(
                                teamId = payload.teamId,
                                organizationId = payload.organizationId
                        ),
                        metadata
                )
        }
}
