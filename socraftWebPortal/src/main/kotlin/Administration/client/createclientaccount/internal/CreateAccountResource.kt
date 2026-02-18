package administration.client.createclientaccount.internal

import administration.client.domain.commands.createclientaccount.CreateAccountCommand
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class CreateClientAccountPayload(
        var clientEmail: String,
        var companyId: Long,
        var connectionId: UUID
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660029388833
*/
@RestController
class CreateAccountResource(private var commandGateway: CommandGateway) {

        var logger = KotlinLogging.logger {}

        @CrossOrigin
        @PostMapping("/debug/createclientaccount")
        fun processDebugCommand(
                @RequestParam clientEmail: String,
                @RequestParam companyId: Long,
                @RequestParam connectionId: UUID,
                @RequestParam clientId: UUID
        ): CompletableFuture<Any> {
                return commandGateway.send(
                        CreateAccountCommand(clientId, clientEmail, companyId, connectionId)
                )
        }

        @CrossOrigin
        @PostMapping("/createclientaccount")
        fun processCommand(
                @RequestBody payload: CreateClientAccountPayload
        ): CompletableFuture<Any> {
                return commandGateway.send(
                        CreateAccountCommand(
                                clientId = UUID.randomUUID(),
                                clientEmail = payload.clientEmail,
                                companyId = payload.companyId,
                                connectionId = payload.connectionId
                        )
                )
        }
}
