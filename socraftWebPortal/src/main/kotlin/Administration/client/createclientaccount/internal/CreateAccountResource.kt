package administration.client.createclientaccount.internal

import administration.client.clientaccountlist.ClientAccountListReadModelEntity
import administration.client.clientaccountlist.ClientAccountListReadModelQuery
import administration.client.domain.commands.createclientaccount.CreateAccountCommand
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class CreateClientAccountPayload(
        var clientEmail: String,
        var companyId: Long,
        var connectionId: UUID
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660029388833
*/
@RestController
@RequestMapping("/client")
class CreateAccountResource(
        private val commandGateway: CommandGateway,
        private val queryGateway: QueryGateway // Added to check for existing email
) {

        private val logger = KotlinLogging.logger {}

        @CrossOrigin
        @PostMapping("/createclientaccount")
        fun processCommand(
                @RequestBody payload: CreateClientAccountPayload
        ): CompletableFuture<Map<String, Any>> {

                // 1. Validation: Check if email already exists in the Read Model
                val query = ClientAccountListReadModelQuery(email = payload.clientEmail)

                val existingAccounts =
                        queryGateway
                                .query(
                                        query,
                                        ResponseTypes.multipleInstancesOf(
                                                ClientAccountListReadModelEntity::class.java
                                        )
                                )
                                .get() // Blocks to ensure we don't create a duplicate

                if (existingAccounts.isNotEmpty()) {
                        // This is the message the user WILL see now
                        throw ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "The email '${payload.clientEmail}' is already associated with an account. Please use a different email."
                        )
                }

                // 2. Execution: Only happens if the list above was empty
                val clientId = UUID.randomUUID()
                return commandGateway.send<Long>(
                                CreateAccountCommand(
                                        clientId = clientId,
                                        clientEmail = payload.clientEmail,
                                        companyId = payload.companyId,
                                        connectionId = payload.connectionId
                                )
                        )
                        .thenApply { companyId ->
                                mapOf("clientId" to clientId, "companyId" to companyId)
                        }
        }

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
}
