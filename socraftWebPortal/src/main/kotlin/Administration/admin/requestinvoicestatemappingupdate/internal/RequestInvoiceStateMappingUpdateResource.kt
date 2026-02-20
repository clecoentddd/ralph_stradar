package administration.admin.requestinvoicestatemappingupdate.internal

import administration.admin.domain.commands.requestinvoicestatemappingupdate.RequestInvoiceStateMappingUpdateCommand
import administration.common.SettingsConstants
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

data class RequestInvoiceStateMappingUpdatePayload(var connectionId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713526
*/
@RestController
@RequestMapping("/admin")
class RequestInvoiceStateMappingUpdateResource(private var commandGateway: CommandGateway) {

  var logger = KotlinLogging.logger {}

  @CrossOrigin
  @PostMapping("/debug/requestinvoicestatemappingupdate")
  fun processDebugCommand(
      @RequestParam settingsId: UUID,
      @RequestParam connectionId: UUID
  ): CompletableFuture<Any> {
    return commandGateway.send(RequestInvoiceStateMappingUpdateCommand(settingsId, connectionId))
  }

  @CrossOrigin
  @PostMapping("/requestinvoicestatemappingupdate")
  fun processCommand(
      @RequestBody payload: RequestInvoiceStateMappingUpdatePayload
  ): CompletableFuture<Any> {
    return commandGateway.send(
        RequestInvoiceStateMappingUpdateCommand(
            SettingsConstants.SETTINGS_ID, connectionId = payload.connectionId))
  }
}
