package stradar.organizationview.changeinitiativeitem.internal

import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.changeinitiativeitem.ChangeInitiativeItemCommand
import stradar.security.SecurityHelper
import stradar.support.metadata.*

data class ChangeInitiativeItemPayload(
        val initiativeId: UUID?,
        val step: String?,
        val itemId: UUID?,
        val content: String?,
        val status: String?
)

@Tag(name = "Initiatives", description = "Endpoints for managing strategy initiative items")
@RestController
class ChangeInitiativeItemResource(
        private val commandGateway: CommandGateway,
        private val securityHelper: SecurityHelper
) {

    @CrossOrigin(
            allowedHeaders =
                    [
                            "Authorization",
                            ORGANIZATION_ID_HEADER,
                            SESSION_ID_HEADER,
                            "X-Correlation-Id",
                            "Content-Type",
                            USER_ID_HEADER]
    )
    @PostMapping("/changeinitiativeitem/{id}")
    fun processCommand(
            @PathVariable("id") pathId: UUID,
            @RequestBody payload: ChangeInitiativeItemPayload,
            @RequestHeader("x-user-id") userId: String,
            @RequestHeader(SESSION_ID_HEADER) sessionId: String,
            @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
            @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
            authentication: Authentication
    ): CompletableFuture<Any> {

        // 🔒 Verify user belongs to the organization in the header
        val user = securityHelper.extractUser(authentication)
        securityHelper.checkOrganization<Any>(user, organizationId)?.let {
            return CompletableFuture.completedFuture(it)
        }

        // Strict validation
        val initiativeId = payload.initiativeId ?: pathId
        val step = requireNotNull(payload.step) { "Field 'step' is mandatory (e.g., DIAGNOSTIC)" }
        val itemId = requireNotNull(payload.itemId) { "Field 'itemId' is mandatory" }
        val content = payload.content ?: ""
        val status = payload.status ?: "ACTIVE"

        val metadata =
                MetaData.with(USER_ID_HEADER, userId)
                        .and("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
                        .and(SESSION_ID_HEADER, sessionId)
                        .and(ORGANIZATION_ID_HEADER, organizationId)

        val command =
                ChangeInitiativeItemCommand(
                        initiativeId = initiativeId,
                        step = step,
                        itemId = itemId,
                        content = content,
                        status = status
                )

        return commandGateway.send(
                GenericCommandMessage.asCommandMessage<ChangeInitiativeItemCommand>(command)
                        .withMetaData(metadata)
        )
    }
}
