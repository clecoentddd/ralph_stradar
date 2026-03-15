package stradar.organizationview.deleteteam.internal

import java.util.UUID
import java.util.concurrent.ExecutionException
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import stradar.common.CommandException
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand
import stradar.support.metadata.*

data class DeleteTeamPayload(
    var teamId: UUID,
    var organizationId: UUID,
    var reason: String? = null
)

@RestController
class DeleteTeamResource(private var commandGateway: CommandGateway) {

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
              ORGANIZATION_ID_HEADER,
              SESSION_ID_HEADER,
              "Content-Type",
              "X-Correlation-Id",
              USER_ID_HEADER])
  @PostMapping("/debug/deleteteam")
  fun processDebugCommand(
      @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
      @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
      @RequestParam teamId: UUID,
      @RequestParam organizationId: UUID,
      @RequestParam(required = false) reason: String?
  ): ResponseEntity<Any> {
    val metadata =
        MetaData.with("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
            .and(SESSION_ID_HEADER, sessionId)
            .and(ORGANIZATION_ID_HEADER, organizationId)
    return dispatchDelete(
        DeleteTeamCommand(
            teamId = teamId, organizationId = organizationId, reason = reason ?: "Debug deletion"),
        metadata)
  }

  @CrossOrigin
  @PostMapping("/deleteteam/{id}")
  fun processCommand(
      @RequestHeader(USER_ID_HEADER) userId: String,
      @RequestHeader(SESSION_ID_HEADER) sessionId: String,
      @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
      @PathVariable("id") teamId: UUID,
      @RequestBody payload: DeleteTeamPayload
  ): ResponseEntity<Any> {
    val metadata =
        MetaData.with(USER_ID_HEADER, userId)
            .and(SESSION_ID_HEADER, sessionId)
            .and("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
            .and(ORGANIZATION_ID_HEADER, payload.organizationId)
    return dispatchDelete(
        DeleteTeamCommand(
            teamId = payload.teamId,
            organizationId = payload.organizationId,
            reason = payload.reason ?: "No reason provided"),
        metadata)
  }
}
