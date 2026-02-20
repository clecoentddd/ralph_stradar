package administration.admin.initializesettings.internal

import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.common.SettingsConstants.SETTINGS_ID
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class InitializesettingsPayload(var connectionId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659756306800
*/
@RestController
@RequestMapping("/admin")
class CreateSettingsResource(private var commandGateway: CommandGateway) {

  var logger = KotlinLogging.logger {}

  @CrossOrigin
  @PostMapping("/debug/initializesettings")
  fun processDebugCommand(
          @RequestParam settingsId: UUID,
          @RequestParam connectionId: UUID
  ): CompletableFuture<Any> {
    return commandGateway.send(CreateSettingsCommand(settingsId, connectionId))
  }

  @CrossOrigin
  @PostMapping("/initializesettings")
  fun processCommand(@RequestBody payload: InitializesettingsPayload): CompletableFuture<Any> {
    return commandGateway.send(
            CreateSettingsCommand(SETTINGS_ID, connectionId = payload.connectionId)
    )
  }
}
