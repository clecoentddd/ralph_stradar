package stradar.organizationview.updateradarelement.internal

import java.util.UUID
import java.util.concurrent.ExecutionException
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import stradar.common.*
import stradar.organizationview.domain.commands.updateenvironmentalchange.UpdateEnvironmentalChangeCommand
import stradar.support.metadata.*

data class UpdateEnvironmentalChangePayload(
    var environmentalChangeId: UUID,
    var teamId: UUID,
    var organizationId: UUID,
    var assess: String,
    var category: ChangeCategory,
    var detect: String,
    var distance: ChangeDistance,
    var impact: ChangeImpact,
    var respond: String,
    var risk: ChangeRisk,
    var title: String,
    var type: ChangeType
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661051289511
*/
@RestController
class UpdateEnvironmentalChangeResource(private var commandGateway: CommandGateway) {

  var logger = KotlinLogging.logger {}

  @CrossOrigin(
      allowedHeaders =
          [
              ORGANIZATION_ID_HEADER,
              SESSION_ID_HEADER,
              "Content-Type",
              "X-Correlation-Id",
              USER_ID_HEADER])
  @PostMapping("/updateenvironmentalchange/{id}")
  fun processCommand(
      @RequestHeader(USER_ID_HEADER) userId: String,
      @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
      @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
      @PathVariable("id") environmentalChangeId: UUID,
      @RequestBody payload: UpdateEnvironmentalChangePayload
  ): ResponseEntity<Any> {

    logger.info {
      "Updating Element ${payload.environmentalChangeId} inside Radar $environmentalChangeId"
    }

    val metadata =
        MetaData.with("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
            .and(SESSION_ID_HEADER, sessionId)
            .and(USER_ID_HEADER, userId)
            .and(ORGANIZATION_ID_HEADER, payload.organizationId)

    return try {
      val result =
          commandGateway
              .send<Any>(
                  UpdateEnvironmentalChangeCommand(
                      environmentalChangeId = environmentalChangeId,
                      teamId = payload.teamId,
                      organizationId = payload.organizationId,
                      assess = payload.assess,
                      category = payload.category,
                      detect = payload.detect,
                      distance = payload.distance,
                      impact = payload.impact,
                      respond = payload.respond,
                      risk = payload.risk,
                      title = payload.title,
                      type = payload.type),
                  metadata)
              .get()
      ResponseEntity.ok(result)
    } catch (ex: ExecutionException) {
      val cause = ex.cause
      logger.warn { "UpdateEnvironmentalChange failed: ${cause?.message ?: ex.message}" }
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
}
