package stradar.platformadministration.signinadmin.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.common.CommandResult
import stradar.platformadministration.domain.commands.signinadmin.SignInAdminCommand
import stradar.support.metadata.*

// Constant for the "Genesis" Super Admin Slot
private val SUPER_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")

data class SigninadminPayload(val username: String)

@RestController
class SignInAdminResource(private val commandGateway: CommandGateway) {

  private val logger = KotlinLogging.logger {}

  @CrossOrigin
  @PostMapping("/signinsuperadmin")
  fun processCommand(
          @RequestBody payload: SigninadminPayload,
          @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
          @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String
  ): CompletableFuture<CommandResult> {

    // 🛡️ Security: Pass the Fixed ID in metadata as the acting 'personId'
    val metadata =
            MetaData.with("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
                    .and(SESSION_ID_HEADER, sessionId)
                    .and(USER_ID_HEADER, SUPER_ADMIN_ID.toString())
                    .and(
                            ORGANIZATION_ID_HEADER,
                            UUID.fromString("00000000-0000-0000-0000-000000000000")
                    ) // System Org

    // 🚀 Command: Target the fixed slot with the provided username
    val command = SignInAdminCommand(adminAccountId = SUPER_ADMIN_ID, username = payload.username)

    logger.info {
      "🔑 Processing Super Admin sign-in for: ${payload.username} [Slot: $SUPER_ADMIN_ID]"
    }

    return commandGateway.send<CommandResult>(command, metadata)
  }
}
