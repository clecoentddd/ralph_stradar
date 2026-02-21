package administration.admin.initializesettings.internal

import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.common.SettingsConstants.SETTINGS_ID
import administration.support.metadata.AdminSecurityHeaders
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*

data class InitializesettingsPayload(var connectionId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822859
*/
@RestController
@RequestMapping("/admin")
class CreateSettingsResource(private var commandGateway: CommandGateway) {

        var logger = KotlinLogging.logger {}

        @CrossOrigin(origins = ["\${app.frontend-url:http://localhost:8081}"])
        @PostMapping("/debug/initializesettings")
        fun processDebugCommand(
                @RequestParam settingsId: UUID,
                @RequestParam connectionId: UUID
        ): CompletableFuture<Any> {
                return commandGateway.send(CreateSettingsCommand(settingsId, connectionId))
        }

        @CrossOrigin(origins = ["\${app.frontend-url:http://localhost:8081}"])
        @PostMapping("/initializesettings")
        fun processCommand(
                @RequestHeader(AdminSecurityHeaders.SESSION_ID) sessionId: String,
                @RequestBody payload: InitializesettingsPayload
        ): CompletableFuture<Any> {
                logger.info {
                        "Processing initialization for connection: ${payload.connectionId} and sessionId: $sessionId"
                }

                val metaData =
                        MetaData.with(AdminSecurityHeaders.SESSION_ID, sessionId)
                                .and(AdminSecurityHeaders.ADMIN_COMPANY_ID, "SOCRAFT_ADMIN_BACKEND")

                val command =
                        CreateSettingsCommand(SETTINGS_ID, connectionId = payload.connectionId)

                return commandGateway.send<Any>(
                        GenericCommandMessage.asCommandMessage<CreateSettingsCommand>(command)
                                .withMetaData(metaData)
                )
        }
}
