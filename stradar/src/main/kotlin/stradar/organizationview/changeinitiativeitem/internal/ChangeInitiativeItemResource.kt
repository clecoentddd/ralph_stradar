package stradar.organizationview.changeinitiativeitem.internal

import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.changeinitiativeitem.ChangeInitiativeItemCommand
import stradar.support.metadata.*

data class ChangeInitiativeItemPayload(
        val initiativeId: UUID?,
        val step: String?,
        val itemId: UUID?,
        val content: String?, // Ensure String for text
        val status: String? // Ensure String for status
)

@Tag(name = "Initiatives", description = "Endpoints for managing strategy initiative items")
@RestController
class ChangeInitiativeItemResource(private val commandGateway: CommandGateway) {

        @CrossOrigin(
                allowedHeaders =
                        [
                                ORGANIZATION_ID_HEADER,
                                SESSION_ID_HEADER,
                                "X-Correlation-Id",
                                "Content-Type",
                                USER_ID_HEADER]
        )
        @PostMapping("/changeinitiativeitem/{id}")
        fun processCommand(
                @PathVariable("id") pathId: UUID, // This is the ID from the URL string
                @RequestBody payload: ChangeInitiativeItemPayload,
                @RequestHeader("x-user-id") userId: String,
                @RequestHeader(SESSION_ID_HEADER) sessionId: String,
                @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(ORGANIZATION_ID_HEADER)
                organizationId: UUID // Renamed for clarity with Aggregate guard
        ): CompletableFuture<Any> {

                // 1. STRICT VALIDATION (Ensure no nulls)
                val initiativeId = payload.initiativeId ?: pathId
                val step =
                        requireNotNull(payload.step) {
                                "Field 'step' is mandatory (e.g., DIAGNOSTIC)"
                        }
                val itemId = requireNotNull(payload.itemId) { "Field 'itemId' is mandatory" }
                val content = payload.content ?: "" // Default to empty string if null
                val status = payload.status ?: "ACTIVE" // Default to ACTIVE if null

                // 2. PREPARE METADATA (Including the mandatory organizationId for the Aggregate)
                val metadata =
                        MetaData.with(USER_ID_HEADER, userId)
                                .and(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and(
                                        ORGANIZATION_ID_HEADER,
                                        organizationId
                                ) // This satisfies the requireNotNull in your Aggregate

                // 3. CONSTRUCT COMMAND
                val command =
                        ChangeInitiativeItemCommand(
                                initiativeId = initiativeId,
                                step = step,
                                itemId = itemId,
                                content = content,
                                status = status
                        )

                // 4. DISPATCH
                return commandGateway.send(
                        GenericCommandMessage.asCommandMessage<ChangeInitiativeItemCommand>(command)
                                .withMetaData(metadata)
                )
        }
}
