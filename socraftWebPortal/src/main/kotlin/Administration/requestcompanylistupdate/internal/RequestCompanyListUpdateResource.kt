package administration.requestcompanylistupdate.internal

import administration.common.SettingsConstants
import administration.domain.commands.requestcompanylistupdate.RequestCompanyListUpdateCommand
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class RequestCompanyListUpdatePayload(var connectionId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822177
*/
@RestController
class RequestCompanyListUpdateResource(private var commandGateway: CommandGateway) {

  var logger = KotlinLogging.logger {}

  @CrossOrigin
  @PostMapping("/debug/requestcompanylistupdate")
  fun processDebugCommand(
      @RequestParam connectionId: UUID,
      @RequestParam settingsId: UUID
  ): CompletableFuture<Any> {
    return commandGateway.send(RequestCompanyListUpdateCommand(connectionId, settingsId))
  }

  @CrossOrigin
  @PostMapping("/requestcompanylistupdate")
  fun processCommand(
      @RequestBody payload: RequestCompanyListUpdatePayload
  ): CompletableFuture<Any> {
    return commandGateway.send(
        RequestCompanyListUpdateCommand(
            settingsId = SettingsConstants.SETTINGS_ID, connectionId = payload.connectionId))
  }
}
