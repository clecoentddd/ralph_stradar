package stradar.organizationview.deleteteam.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand
import stradar.support.metadata.SESSION_ID_HEADER

// 1. Update the payload to accept the reason from the frontend
data class DeleteTeamPayload(
        var teamId: UUID,
        var organizationId: UUID,
        var reason: String? = null // Optional: defaults to null if not provided
)

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
                @RequestParam organizationId: UUID,
                @RequestParam(required = false) reason: String? // Added for debug
        ): CompletableFuture<Any> {
                val metadata =
                        MetaData.with(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and("organizationId", organizationId)

                return commandGateway.send(
                        DeleteTeamCommand(
                                teamId = teamId,
                                organizationId = organizationId,
                                reason = reason ?: "Debug deletion"
                        ),
                        metadata
                )
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

                // 2. Pass the reason from the payload into the command
                return commandGateway.send(
                        DeleteTeamCommand(
                                teamId = payload.teamId,
                                organizationId = payload.organizationId,
                                reason = payload.reason ?: "No reason provided"
                        ),
                        metadata
                )
        }
}
