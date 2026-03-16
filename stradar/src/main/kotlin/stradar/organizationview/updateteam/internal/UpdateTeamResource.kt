package stradar.organizationview.updateteam.internal

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
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand
import stradar.security.SecurityHelper
import stradar.support.metadata.*

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
class UpdateTeamResource(
        private var commandGateway: CommandGateway,
        private val securityHelper: SecurityHelper
) {

  var logger = KotlinLogging.logger {}

  private fun dispatchUpdate(command: UpdateTeamCommand, metadata: MetaData): ResponseEntity<Any> {
    return try {
      val result = commandGateway.send<Any>(command, metadata).get()
      ResponseEntity.ok(result)
    } catch (ex: ExecutionException) {
      val cause = ex.cause
      logger.warn { "UpdateTeam failed: ${cause?.message ?: ex.message}" }
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
  @PostMapping("/updateteam/{id}")
  fun processCommand(
          @RequestHeader(USER_ID_HEADER) userId: String,
          @RequestHeader(SESSION_ID_HEADER) sessionId: String,
          @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
          @PathVariable("id") teamId: UUID,
          @RequestBody payload: UpdateTeamPayload,
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

    return dispatchUpdate(
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
