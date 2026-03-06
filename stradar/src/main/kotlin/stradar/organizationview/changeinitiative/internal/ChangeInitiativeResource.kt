package stradar.organizationview.changeinitiative.internal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.changeinitiative.ChangeInitiativeCommand
import stradar.support.metadata.SESSION_ID_HEADER

/** The Request Body structure */
data class ChangeInitiativePayload(
        val initiativeId: UUID,
        val initiativeName: String,
        val organizationId: UUID,
        val status: String
)

@Tag(name = "Initiatives", description = "Endpoints for managing overall Initiative properties")
@RestController
class ChangeInitiativeResource(private val commandGateway: CommandGateway) {

        private val logger = KotlinLogging.logger {}

        @Operation(
                summary = "Update an existing Initiative",
                description =
                        "Changes the name, organization, or status of an existing initiative record."
        )
        @CrossOrigin(
                allowedHeaders =
                        [
                                "organizationId",
                                SESSION_ID_HEADER,
                                "Content-Type",
                                "X-Correlation-Id",
                                "x-user-id"]
        )
        @PostMapping("/changeinitiative/{initiativeId}")
        fun processCommand(
                @PathVariable("initiativeId") initiativeId: UUID,
                @RequestBody payload: ChangeInitiativePayload, // Uses the data class above
                @RequestHeader("organizationId") organizationId: UUID,
                @RequestHeader("x-user-id") userId: String,
                @RequestHeader("x-session-id") sessionId: String,
                @RequestHeader("X-Correlation-Id", required = false) correlationId: String?
        ): CompletableFuture<Any> {

                // 1. Consistency Check
                require(initiativeId == payload.initiativeId) { "ID Mismatch" }

                // 2. Build MetaData manually (Writing side)
                val metadata =
                        MetaData.with("organizationId", organizationId)
                                .and("x-user-id", userId)
                                .and(SESSION_ID_HEADER, sessionId)
                                .and(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )

                // 3. Dispatch Command
                val command =
                        ChangeInitiativeCommand(
                                initiativeId = initiativeId,
                                initiativeName = payload.initiativeName,
                                organizationId = organizationId,
                                status = payload.status
                        )

                return commandGateway.send(command, metadata)
        }
}
