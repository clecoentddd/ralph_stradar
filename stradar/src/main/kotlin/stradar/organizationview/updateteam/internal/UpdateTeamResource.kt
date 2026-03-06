package stradar.organizationview.updateteam.internal

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
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand
import stradar.support.metadata.SESSION_ID_HEADER

data class UpdateTeamPayload(
        var teamId: UUID,
        var context: String,
        var level: Int,
        var name: String,
        var organizationId: UUID,
        var purpose: String
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631265233
*/
@RestController
class UpdateTeamResource(private var commandGateway: CommandGateway) {

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
        @PostMapping("/debug/updateteam")
        fun processDebugCommand(
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestParam teamId: UUID,
                @RequestParam context: String,
                @RequestParam level: Int,
                @RequestParam name: String,
                @RequestParam organizationId: UUID,
                @RequestParam purpose: String
        ): CompletableFuture<Any> {
                val metadata =
                        MetaData.with(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and("organizationId", organizationId)

                return commandGateway.send(
                        UpdateTeamCommand(teamId, context, level, name, organizationId, purpose),
                        metadata
                )
        }

        @CrossOrigin
        @PostMapping("/updateteam/{id}")
        fun processCommand(
                @RequestHeader("x-user-id") userId: String,
                @RequestHeader(SESSION_ID_HEADER) sessionId: String,
                @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
                @PathVariable("id") teamId: UUID,
                @RequestBody payload: UpdateTeamPayload
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
                        UpdateTeamCommand(
                                teamId = payload.teamId,
                                context = payload.context,
                                level = payload.level,
                                name = payload.name,
                                organizationId = payload.organizationId,
                                purpose = payload.purpose
                        ),
                        metadata
                )
        }
}
