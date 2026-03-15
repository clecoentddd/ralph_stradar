package stradar.platformadministration.defineorganization.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import stradar.common.CommandResult
import stradar.platformadministration.domain.commands.defineorganization.DefineOrganizationCommand
import stradar.support.metadata.*

/**
 * Payload with internal validation logic to ensure no nulls or empty strings reach the command bus.
 */
data class DefineOrganizationPayload(
        val organizationId: UUID?,
        val organizationUserId: UUID?,
        val organizationUserEmail: String?,
        val organizationName: String?,
        val role: String?
) {
  fun validate() {
    requireNotNull(organizationId) { "organizationId is required and must be a valid UUID" }
    requireNotNull(organizationUserId) { "organizationUserId is required and must be a valid UUID" }
    require(!organizationUserEmail.isNullOrBlank()) {
      "organizationUserEmail is required and cannot be empty"
    }
    require(!organizationName.isNullOrBlank()) {
      "organizationName is required and cannot be empty"
    }
    require(!role.isNullOrBlank()) { "role is required and cannot be empty" }
  }
}

@RestController
class DefineOrganizationResource(
        private val commandGateway: CommandGateway,
        private val queryGateway: QueryGateway
) {

  private val logger = KotlinLogging.logger {}

  @CrossOrigin
  @PostMapping("/defineorganization")
  fun processCommand(
          @RequestHeader(SESSION_ID_HEADER) sessionId: String,
          @RequestHeader(USER_ID_HEADER, required = false, defaultValue = "\${user.name}")
          userId: String,
          @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
          @RequestBody payload: DefineOrganizationPayload
  ): CompletableFuture<ResponseEntity<CommandResult>> {

    // 🛡️ 1. Immediate Validation of Payload
    // Throws IllegalArgumentException (400 Bad Request) if any field is missing or
    // blank
    payload.validate()

    // 📦 3. Build Metadata Baton
    val metadata =
            MetaData.with(SESSION_ID_HEADER, sessionId)
                    .and(USER_ID_HEADER, userId)
                    .and("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
                    .and(ORGANIZATION_ID_HEADER, payload.organizationId)

    // 🚀 4. Construct and Dispatch Command
    val command =
            DefineOrganizationCommand(
                    organizationId = payload.organizationId!!,
                    organizationUserId = payload.organizationUserId!!,
                    organizationUserEmail = payload.organizationUserEmail!!,
                    organizationName = payload.organizationName!!,
                    role = payload.role!!
            )

    logger.info {
      "User [$userId] is defining organization: ${payload.organizationName} with Admin: ${payload.organizationUserEmail}"
    }

    return commandGateway.send<CommandResult>(command, metadata).thenApply { result ->
      ResponseEntity.ok(result)
    }
  }
}
