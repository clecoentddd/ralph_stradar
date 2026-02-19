package administration.adminconnection.internal

import administration.domain.commands.adminconnection.ToConnectCommand
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class AdminConnectionPayload(var email: String)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734675975
*/
@RestController
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

  @CrossOrigin
  @PostMapping("/adminconnection")
  fun processCommand(
          @RequestBody payload: AdminConnectionPayload
  ): CompletableFuture<Map<String, Any>> {
    val connectionId = UUID.randomUUID()
    return commandGateway.send<Any>(
                    ToConnectCommand(connectionId = connectionId, email = payload.email)
            )
            .thenApply { mapOf("connectionId" to connectionId) }
  }
}
