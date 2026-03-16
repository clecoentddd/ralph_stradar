package stradar.organizationview.changeinitiative.internal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.changeinitiative.ChangeInitiativeCommand
import stradar.security.SecurityHelper
import stradar.support.metadata.*

/** The Request Body structure */
data class ChangeInitiativePayload(
        val initiativeId: UUID,
        val initiativeName: String,
        val organizationId: UUID,
        val status: String
)

@Tag(name = "Initiatives", description = "Endpoints for managing overall Initiative properties")
@RestController
class ChangeInitiativeResource(
        private val commandGateway: CommandGateway,
        private val securityHelper: SecurityHelper
) {

    private val logger = KotlinLogging.logger {}

    @Operation(
            summary = "Update an existing Initiative",
            description =
                    "Changes the name, organization, or status of an existing initiative record."
    )
    @CrossOrigin(
            allowedHeaders =
                    [
                            "Authorization",
                            ORGANIZATION_ID_HEADER,
                            SESSION_ID_HEADER,
                            "Content-Type",
                            "X-Correlation-Id",
                            USER_ID_HEADER]
    )
    @PostMapping("/changeinitiative/{initiativeId}")
    fun processCommand(
            @PathVariable("initiativeId") initiativeId: UUID,
            @RequestBody payload: ChangeInitiativePayload,
            @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
            @RequestHeader(USER_ID_HEADER) userId: String,
            @RequestHeader(SESSION_ID_HEADER) sessionId: String,
            @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
            authentication: Authentication
    ): CompletableFuture<Any> {

        // 🔒 Verify user belongs to the organization in the payload
        val user = securityHelper.extractUser(authentication)
        securityHelper.checkOrganization<Any>(user, organizationId)?.let {
            return CompletableFuture.completedFuture(it)
        }

        // Consistency Check
        require(initiativeId == payload.initiativeId) { "ID Mismatch" }

        val metadata =
                MetaData.with(ORGANIZATION_ID_HEADER, organizationId)
                        .and(USER_ID_HEADER, userId)
                        .and(SESSION_ID_HEADER, sessionId)
                        .and("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())

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
