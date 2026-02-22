package administration.client.createclientaccount.internal

import administration.client.clientaccountlist.ClientAccountListReadModel
import administration.client.clientaccountlist.ClientAccountListReadModelQuery
import administration.client.domain.commands.createclientaccount.CreateAccountCommand
import administration.support.metadata.AppSecurityHeaders
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class CreateClientAccountPayload(
        var clientEmail: String,
        var companyId: Long,
        var connectionId: UUID
)

@RestController
@RequestMapping("/client")
class CreateAccountResource(
        private val commandGateway: CommandGateway,
        private val queryGateway: QueryGateway
) {

        private val logger = KotlinLogging.logger {}

        @CrossOrigin
        @PostMapping("/createclientaccount")
        fun processCommand(
                @RequestHeader(AppSecurityHeaders.SESSION_ID_HEADER) sessionId: String,
                @RequestHeader(AppSecurityHeaders.COMPANY_ID_HEADER) companyId: String,
                @RequestBody payload: CreateClientAccountPayload
        ): CompletableFuture<Map<String, Any>> {

                val query = ClientAccountListReadModelQuery(email = payload.clientEmail)

                return queryGateway
                        .query(
                                query,
                                ResponseTypes.multipleInstancesOf(
                                        ClientAccountListReadModel::class.java
                                )
                        )
                        .thenCompose { existingAccounts ->
                                if (existingAccounts.isNotEmpty()) {
                                        throw ResponseStatusException(
                                                HttpStatus.CONFLICT,
                                                "Email already exists"
                                        )
                                }

                                val clientId = UUID.randomUUID()

                                val metaData =
                                        MetaData.with(
                                                        AppSecurityHeaders.SESSION_ID_HEADER,
                                                        sessionId
                                                )
                                                .and(
                                                        AppSecurityHeaders.COMPANY_ID_HEADER,
                                                        "SOCRAFT_ADMIN_BACKEND"
                                                )

                                commandGateway.send<Long>(
                                                CreateAccountCommand(
                                                        clientId = clientId,
                                                        clientEmail = payload.clientEmail,
                                                        companyId = payload.companyId,
                                                        connectionId = payload.connectionId
                                                ),
                                                metaData
                                        )
                                        .thenApply { companyId ->
                                                mapOf<String, Any>(
                                                        "clientId" to clientId,
                                                        "companyId" to companyId
                                                )
                                        }
                        }
                        .exceptionally { throwable ->
                                // This block will catch the Interceptor's IllegalArgumentException
                                logger.error {
                                        "Interceptor blocked the command: ${throwable.message}"
                                }
                                throw ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Security Block: ${throwable.localizedMessage}"
                                )
                        }
        }
}
