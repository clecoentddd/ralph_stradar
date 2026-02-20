package administration.client.clientaccountconnection.internal

import administration.client.domain.commands.clientaccountconnection.ToConnectToAccountCommand
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class ClientAccountConnectionPayload(var clientEmail: String, var clientId: UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660036569904
*/
@RestController
@RequestMapping("/client")
class ToConnectToAccountResource(private var commandGateway: CommandGateway) {

  var logger = KotlinLogging.logger {}

  @CrossOrigin
  @PostMapping("/debug/clientaccountconnection")
  fun processDebugCommand(
          @RequestParam clientEmail: String,
          @RequestParam clientId: UUID
  ): CompletableFuture<Any> {
    return commandGateway.send(ToConnectToAccountCommand(clientEmail, clientId))
  }

  @CrossOrigin
  @PostMapping("/clientaccountconnection/{id}")
  fun processCommand(
          @PathVariable("id") clientId: UUID,
          @RequestBody payload: ClientAccountConnectionPayload
  ): CompletableFuture<Map<String, Any>> {
    return commandGateway.send<Long>(
                    ToConnectToAccountCommand(
                            clientId = clientId,
                            clientEmail = payload.clientEmail
                    )
            )
            .thenApply { mapOf("companyId" to it) }
  }
}
