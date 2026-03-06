package stradar.organizationview.createinitiative.internal

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.createinitiative.CreateInitiativeCommand

// Constant for your session header
const val SESSION_ID_HEADER = "X-Session-Id"

data class CreateInitiativePayload(
        @field:Schema(example = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d") var initiativeId: UUID,
        @field:Schema(example = "Modernize Legacy Infrastructure") var initiativeName: String,
        @field:Schema(example = "474e4828-a953-4240-bb26-368bb332398e") var organizationId: UUID,
        @field:Schema(example = "77777777-7777-7777-7777-777777777777") var strategyId: UUID,
        @field:Schema(example = "18ed5446-4fc6-4dd5-8e98-5b9c5cbf130d") var teamId: UUID
)

@RestController
class CreateInitiativeResource(private var commandGateway: CommandGateway) {

        private val logger = KotlinLogging.logger {}

        @CrossOrigin(
                allowedHeaders =
                        [
                                "organizationId",
                                "X-Session-Id",
                                "X-Correlation-Id",
                                "Content-Type",
                                "x-user-id"]
        )
        @PostMapping("/createinitiative/{id}")
        fun processCommand(
                @PathVariable("id") initiativeId: UUID,
                @RequestHeader("x-user-id") userId: String,
                @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(SESSION_ID_HEADER) sessionId: String,
                @RequestBody payload: CreateInitiativePayload
        ): CompletableFuture<Any> {

                // ── YOUR EXACT METADATA CONTRACT ──
                val metadata =
                        MetaData.with("x-user-id", userId)
                                .and(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and("organizationId", payload.organizationId)

                logger.info {
                        "Dispatching Initiative for $initiativeId [User: $userId, Session: $sessionId, Org: ${payload.organizationId}]"
                }

                val command =
                        CreateInitiativeCommand(
                                initiativeId = initiativeId,
                                initiativeName = payload.initiativeName,
                                organizationId = payload.organizationId,
                                strategyId = payload.strategyId,
                                teamId = payload.teamId
                        )

                // Wrap the command with the metadata
                return commandGateway.send<Any>(
                        GenericCommandMessage.asCommandMessage<CreateInitiativeCommand>(command)
                                .withMetaData(metadata)
                )
        }
}
