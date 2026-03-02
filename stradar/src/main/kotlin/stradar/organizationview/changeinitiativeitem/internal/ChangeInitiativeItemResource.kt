package stradar.organizationview.changeinitiativeitem.internal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.changeinitiativeitem.ChangeInitiativeItemCommand
import stradar.support.metadata.SESSION_ID_HEADER

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

    private val logger = KotlinLogging.logger {}

    @Operation(
            summary = "Update or add an item to an initiative",
            description = "Changes a specific item in steps like DIAGNOSTIC or OVERALLAPPROACH"
    )
    @CrossOrigin
    @PostMapping("/changeinitiativeitem/{id}")
    fun processCommand(
            @Parameter(description = "The UUID of the Initiative") @PathVariable("id") pathId: UUID,
            @RequestBody payload: ChangeInitiativeItemPayload,

            // Example of extracting metadata from headers
            @RequestHeader("x-user-id") userId: String,
            @RequestHeader("x-session-id") sessionId: String,
            @RequestHeader("X-Correlation-Id", required = false) correlationId: String?
    ): CompletableFuture<Any> {

        // 1. Validation
        val initiativeId = payload.initiativeId ?: pathId
        val step = requireNotNull(payload.step) { "Step is mandatory" }
        val itemId = requireNotNull(payload.itemId) { "itemId is mandatory" }

        // 2. Prepare Metadata
        val metadata =
                MetaData.with("x-user-id", userId)
                        .and("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
                        .and(SESSION_ID_HEADER, sessionId)

        logger.info {
            "Dispatching Initiative for $initiativeId [User: $userId, Session: $sessionId]"
        }

        // 3. Create the Command Message
        val command =
                ChangeInitiativeItemCommand(
                        initiativeId = initiativeId,
                        step = step,
                        itemId = itemId,
                        content = payload.content ?: "",
                        status = payload.status ?: "ACTIVE"
                )

        // Sending the command wrapped with the metadata
        return commandGateway.send(
                GenericCommandMessage.asCommandMessage<ChangeInitiativeItemCommand>(command)
                        .withMetaData(metadata)
        )
    }
}
