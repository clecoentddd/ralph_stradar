package administration.client.clientaccountconnection.internal

import administration.client.clientaccountlist.*
import administration.client.domain.commands.clientaccountconnection.ToConnectToAccountCommand
import administration.support.metadata.AppSecurityHeaders
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class ClientAccountConnectionPayload(val clientEmail: String, val clientId: UUID)

@RestController
@RequestMapping("/client")
@CrossOrigin
class ToConnectToAccountResource(
        private val commandGateway: CommandGateway,
        private val queryGateway: QueryGateway
) {
        @PostMapping("/debug/clientaccountconnection")
        fun processDebugCommand(
                @RequestParam clientEmail: String,
                @RequestParam clientId: UUID
        ): CompletableFuture<Any> {
                return commandGateway.send(ToConnectToAccountCommand(clientEmail, clientId))
        }

        @PostMapping("/clientaccountconnection/{id}")
        fun processCommand(
                @PathVariable("id") clientId: UUID,
                @RequestBody payload: ClientAccountConnectionPayload,
                @RequestHeader(AppSecurityHeaders.SESSION_ID_HEADER) sessionId: String,
                @RequestHeader(AppSecurityHeaders.COMPANY_ID_HEADER) companyId: String
        ): CompletableFuture<Map<String, Any>> {

                val companyIdLong = companyId.toLongOrNull()
                val query =
                        ClientAccountListReadModelQuery(
                                email = payload.clientEmail,
                                companyId = companyIdLong
                        )

                // We must specify the List return type for multipleInstancesOf
                return queryGateway.query(
                                query,
                                ResponseTypes.multipleInstancesOf(
                                        ClientAccountListReadModel::class.java
                                )
                        )
                        .thenCompose { accounts ->
                                val account =
                                        accounts.firstOrNull()
                                                ?: throw ResponseStatusException(
                                                        HttpStatus.NOT_FOUND,
                                                        "Client account not found"
                                                )

                                val metaData =
                                        MetaData.with(
                                                        AppSecurityHeaders.SESSION_ID_HEADER,
                                                        sessionId
                                                )
                                                .and(
                                                        AppSecurityHeaders.COMPANY_ID_HEADER,
                                                        account.companyId?.toString() ?: "UNKNOWN"
                                                )

                                // Ensure parameter names match your ToConnectToAccountCommand
                                // exactly
                                val command =
                                        ToConnectToAccountCommand(
                                                clientEmail = payload.clientEmail,
                                                clientId = clientId
                                        )

                                commandGateway.send<Any>(command, metaData).thenApply { result ->
                                        mapOf("companyId" to (result ?: "SUCCESS"))
                                }
                        }
        }
}
