package stradar.organizationview.updatestrategy.internal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
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
import stradar.common.StrategyStatus
import stradar.organizationview.domain.commands.updatestrategy.UpdateStrategyCommand
import stradar.security.SecurityHelper
import stradar.support.metadata.*

/** Payload for updating an existing strategy. */
data class UpdateStrategyPayload(
        @Schema(
                description = "The team this strategy belongs to",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        val teamId: UUID,
        @Schema(description = "The organization context") val organizationId: UUID,
        @Schema(description = "The title of the strategy", example = "2026 Growth Plan")
        val title: String,
        @Schema(description = "The timeframe for this strategy", example = "Q1-Q4 2026")
        val timeframe: String,
        @Schema(description = "The status of the strategy", example = "DRAFT") val status: String
)

@RestController
@RequestMapping("/api/v1/strategies")
class UpdateStrategyResource(
        private val commandGateway: CommandGateway,
        private val securityHelper: SecurityHelper
) {

  private val logger = KotlinLogging.logger {}

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
  @PutMapping("/{strategyId}")
  @Operation(summary = "Update an existing strategy")
  fun updateStrategy(
          @PathVariable strategyId: UUID,
          @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
          @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
          @RequestHeader(value = USER_ID_HEADER, required = true) userId: String,
          @RequestBody payload: UpdateStrategyPayload,
          authentication: Authentication
  ): ResponseEntity<Any> {

    // 🔒 Verify user belongs to the organization in the payload
    val user = securityHelper.extractUser(authentication)
    securityHelper.checkOrganization<Any>(user, payload.organizationId)?.let {
      return it
    }

    val strategyBuilderId = "${payload.teamId}-STRATEGY-BUILDER"

    val newStrategyStatus =
            try {
              StrategyStatus.valueOf(payload.status.uppercase())
            } catch (ex: IllegalArgumentException) {
              throw IllegalArgumentException("Invalid strategy status: ${payload.status}")
            }

    logger.info {
      "Updating strategy $strategyId for builder $strategyBuilderId with status $newStrategyStatus"
    }

    val metadata =
            MetaData.with(USER_ID_HEADER, userId)
                    .and("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
                    .and(SESSION_ID_HEADER, sessionId)
                    .and(ORGANIZATION_ID_HEADER, payload.organizationId)

    val command =
            UpdateStrategyCommand(
                    strategyBuilderId = strategyBuilderId,
                    teamId = payload.teamId,
                    organizationId = payload.organizationId,
                    strategyId = strategyId,
                    strategyName = payload.title,
                    strategyTimeframe = payload.timeframe,
                    strategyStatus = newStrategyStatus
            )

    return try {
      val result = commandGateway.send<Any>(command, metadata).get()
      ResponseEntity.ok(result)
    } catch (ex: ExecutionException) {
      val cause = ex.cause
      logger.warn { "UpdateStrategy failed: ${cause?.message ?: ex.message}" }
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
