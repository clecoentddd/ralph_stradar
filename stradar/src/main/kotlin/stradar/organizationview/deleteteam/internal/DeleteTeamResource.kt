package stradar.organizationview.deleteteam.internal

import java.util.UUID
import java.util.concurrent.ExecutionException
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import stradar.common.CommandException
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand
import stradar.security.SecurityHelper
import stradar.support.metadata.*

data class DeleteTeamPayload(
        var teamId: UUID,
        var organizationId: UUID,
        var reason: String? = null
)

@RestController
class DeleteTeamResource(
        private var commandGateway: CommandGateway,
        private val securityHelper: SecurityHelper
) {

  var logger = KotlinLogging.logger {}

  private fun dispatchDelete(command: DeleteTeamCommand, metadata: MetaData): ResponseEntity<Any> {
    return try {
      val result = commandGateway.send<Any>(command, metadata).get()
      ResponseEntity.ok(result)
    } catch (ex: ExecutionException) {
      val cause = ex.cause
      logger.warn { "DeleteTeam failed: ${cause?.message ?: ex.message}" }
      when {
        cause is CommandExecutionException && cause.cause is CommandException ->
                throw cause.cause as CommandException
        cause is CommandException -> throw cause
        cause is CommandExecutionException ->
                throw CommandException(cause.message ?: "Command execution failed")
        cause != null -> throw cause
        else -> throw ex
      }
    }
  }

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
  @PostMapping("/deleteteam/{id}")
  fun processCommand(
          @RequestHeader(USER_ID_HEADER) userId: String,
          @RequestHeader(SESSION_ID_HEADER) sessionId: String,
          @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
          @PathVariable("id") teamId: UUID,
          @RequestBody payload: DeleteTeamPayload,
          authentication: Authentication
  ): ResponseEntity<Any> {

    // 🔒 Verify user belongs to the organization in the payload
    val user = securityHelper.extractUser(authentication)
    securityHelper.checkOrganization<Any>(user, payload.organizationId)?.let {
      return it
    }

    val metadata =
            MetaData.with(USER_ID_HEADER, userId)
                    .and(SESSION_ID_HEADER, sessionId)
                    .and("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
                    .and(ORGANIZATION_ID_HEADER, payload.organizationId)

    return dispatchDelete(
            DeleteTeamCommand(
                    teamId = payload.teamId,
                    organizationId = payload.organizationId,
                    reason = payload.reason ?: "No reason provided"
            ),
            metadata
    )
  }
}
