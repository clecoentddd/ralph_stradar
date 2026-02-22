package administration.admin.adminconnection.internal

import administration.admin.domain.commands.adminconnection.ToConnectCommand
import administration.support.metadata.AppSecurityHeaders
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
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

        @CrossOrigin(origins = ["\${app.frontend-url:http://localhost:8081}"])
        @PostMapping("/adminconnection")
        fun processCommand(
                @RequestHeader(AppSecurityHeaders.SESSION_ID_HEADER) sessionId: String,
                @RequestBody payload: AdminConnectionPayload
        ): CompletableFuture<Map<String, Any>> {

                logger.info {
                        "Processing command for email: ${payload.email} and sessionId: $sessionId"
                }

                val connectionId = UUID.randomUUID()

                // 1. Prepare the metadata
                val metaData =
                        MetaData.with(AppSecurityHeaders.SESSION_ID_HEADER, sessionId)
                                .and(AppSecurityHeaders.COMPANY_ID_HEADER, "SOCRAFT_ADMIN_BACKEND")

                val command = ToConnectCommand(connectionId = connectionId, email = payload.email)

                // 2. FIXED: Send the command AND the metadata
                // CommandGateway.send(payload, metadata) is the standard Axon way
                return commandGateway.send<Any>(command, metaData).thenApply {
                        mapOf("connectionId" to connectionId)
                }
        }

        // DEBUG ENDPOINT: This should now FAIL if you don't provide headers
        @CrossOrigin(origins = ["\${app.frontend-url:http://localhost:8081}"])
        @PostMapping("/debug/adminconnection")
        fun processDebugCommand(
                @RequestParam connectionId: UUID,
                @RequestParam email: String
        ): CompletableFuture<Any> {
                // This sends NO metadata. The interceptor should now catch this and THROW an
                // exception.
                return commandGateway.send(ToConnectCommand(connectionId, email))
        }
}
