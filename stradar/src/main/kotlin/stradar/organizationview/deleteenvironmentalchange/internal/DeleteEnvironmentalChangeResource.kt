package stradar.organizationview.deleteradarelement.internal

import java.util.UUID
import java.util.concurrent.ExecutionException
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import stradar.common.CommandException
import stradar.organizationview.domain.commands.deleteenvironmentalchange.DeleteEnvironmentalChangeCommand
import stradar.support.metadata.*

data class DeleteEnvironmentalChangePayload(
    var environmentalChangeId: UUID,
    var teamId: UUID,
    var organizationId: UUID,
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661052478597
*/
@RestController
class DeleteEnvironmentalChangeResource(private var commandGateway: CommandGateway) {

  var logger = KotlinLogging.logger {}

  private fun dispatchDelete(
      command: DeleteEnvironmentalChangeCommand,
      metadata: MetaData
  ): ResponseEntity<Any> {
    return try {
      val result = commandGateway.send<Any>(command, metadata).get()
      ResponseEntity.ok(result)
    } catch (ex: ExecutionException) {
      val cause = ex.cause
      logger.warn { "DeleteEnvironmentalChange failed: ${cause?.message ?: ex.message}" }
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
  @PostMapping("/debug/deleteenvironmentalchange")
  fun processDebugCommand(
      @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
      @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
      @RequestParam environmentalChangeId: UUID,
      @RequestParam teamId: UUID,
      @RequestParam organizationId: UUID
  ): ResponseEntity<Any> {
    val metadata =
        MetaData.with("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
            .and(SESSION_ID_HEADER, sessionId)
            .and(ORGANIZATION_ID_HEADER, organizationId)
    return dispatchDelete(
        DeleteEnvironmentalChangeCommand(environmentalChangeId, teamId, organizationId), metadata)
  }

  @CrossOrigin(
      allowedHeaders =
          [
              ORGANIZATION_ID_HEADER,
              SESSION_ID_HEADER,
              "Content-Type",
              "X-Correlation-Id",
              USER_ID_HEADER])
  @PostMapping("/deleteenvironmentalchange/{id}")
  fun processCommand(
      @RequestHeader(USER_ID_HEADER) userId: String,
      @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
      @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
      @PathVariable("id") environmentalChangeId: UUID,
      @RequestBody payload: DeleteEnvironmentalChangePayload
  ): ResponseEntity<Any> {
    val metadata =
        MetaData.with("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
            .and(SESSION_ID_HEADER, sessionId)
            .and(USER_ID_HEADER, userId)
            .and(ORGANIZATION_ID_HEADER, payload.organizationId)
    return dispatchDelete(
        DeleteEnvironmentalChangeCommand(
            environmentalChangeId = environmentalChangeId,
            teamId = payload.teamId,
            organizationId = payload.organizationId,
        ),
        metadata)
  }
}
