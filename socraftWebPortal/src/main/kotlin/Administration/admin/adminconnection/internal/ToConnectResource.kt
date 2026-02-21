package administration.admin.adminconnection.internal

import administration.admin.domain.commands.adminconnection.ToConnectCommand
import administration.support.metadata.AdminSecurityHeaders
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class AdminConnectionPayload(var email: String)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734675975
*/
@RestController
@RequestMapping("/admin")
class ToConnectResource(private var commandGateway: CommandGateway) {

        var logger = KotlinLogging.logger {}

        @CrossOrigin
        @PostMapping("/debug/adminconnection")
        fun processDebugCommand(
                @RequestParam connectionId: UUID,
                @RequestParam email: String
        ): CompletableFuture<Any> {
                return commandGateway.send(ToConnectCommand(connectionId, email))
        }

        @CrossOrigin(
                origins = ["\${app.frontend-url:http://localhost:8081}"],
                allowedHeaders = ["*"],
                methods = [RequestMethod.POST, RequestMethod.OPTIONS]
        )
        @PostMapping("/adminconnection")
        fun processCommand(
                @RequestHeader(AdminSecurityHeaders.SESSION_ID) sessionId: String,
                @RequestBody payload: AdminConnectionPayload
        ): CompletableFuture<Map<String, Any>> {
                logger.info {
                        "Processing command for email: ${payload.email} and sessionId: $sessionId"
                }
                val connectionId = UUID.randomUUID()
                val metaData =
                        MetaData.with(AdminSecurityHeaders.SESSION_ID, sessionId)
                                .and(AdminSecurityHeaders.ADMIN_COMPANY_ID, "SOCRAFT_ADMIN_BACKEND")

                val command = ToConnectCommand(connectionId = connectionId, email = payload.email)

                return commandGateway.send<Any>(
                                GenericCommandMessage.asCommandMessage<ToConnectCommand>(command)
                                        .withMetaData(metaData)
                        )
                        .thenApply { mapOf("connectionId" to connectionId) }
        }
}
