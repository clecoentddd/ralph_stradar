package stradar.organizationview.detectenvironmentalchange.internal

import java.util.UUID
import java.util.concurrent.ExecutionException
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import stradar.common.*
import stradar.organizationview.domain.commands.detectenvironmentalchange.DetectEnvironmentalChangeCommand
import stradar.security.SecurityHelper
import stradar.support.metadata.*

data class DetectEnvironmentalChangePayload(
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
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661044303442
*/
@RestController
class DetectEnvironmentalChangeResource(
        private var commandGateway: CommandGateway,
        private val securityHelper: SecurityHelper
) {

    var logger = KotlinLogging.logger {}

    private fun dispatchDetect(
            command: DetectEnvironmentalChangeCommand,
            metadata: MetaData
    ): ResponseEntity<Any> {
        return try {
            val result = commandGateway.send<Any>(command, metadata).get()
            ResponseEntity.ok(result)
        } catch (ex: ExecutionException) {
            val cause = ex.cause
            logger.warn { "DetectEnvironmentalChange failed: ${cause?.message ?: ex.message}" }
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
    @PostMapping("/detectenvironmentalchange")
    fun processCommand(
            @RequestHeader(USER_ID_HEADER) userId: String,
            @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
            @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
            @RequestBody payload: DetectEnvironmentalChangePayload,
            authentication: Authentication
    ): ResponseEntity<Any> {

        // 🔒 Verify user belongs to the organization in the payload
        val user = securityHelper.extractUser(authentication)
        securityHelper.checkOrganization<Any>(user, payload.organizationId)?.let {
            return it
        }

        val metadata =
                MetaData.with("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
                        .and(SESSION_ID_HEADER, sessionId)
                        .and(USER_ID_HEADER, userId)
                        .and(ORGANIZATION_ID_HEADER, payload.organizationId)

        return dispatchDetect(
                DetectEnvironmentalChangeCommand(
                        environmentalChangeId = payload.environmentalChangeId,
                        teamId = payload.teamId,
                        assess = payload.assess,
                        category = payload.category,
                        detect = payload.detect,
                        distance = payload.distance,
                        impact = payload.impact,
                        organizationId = payload.organizationId,
                        respond = payload.respond,
                        risk = payload.risk,
                        title = payload.title,
                        type = payload.type
                ),
                metadata
        )
    }
}
