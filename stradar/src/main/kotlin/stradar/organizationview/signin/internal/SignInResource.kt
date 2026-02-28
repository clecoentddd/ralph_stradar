package stradar.organizationview.signin.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.signin.SignInCommand
import stradar.support.metadata.SESSION_ID_HEADER

data class SignInPayload(val personId: UUID)

@RestController
class SignInResource(private val commandGateway: CommandGateway) {

        private val logger = KotlinLogging.logger {}

        @CrossOrigin
        @PostMapping(value = ["/signin", "/signintoorganizationpersonaccount"])
        fun processCommand(
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestBody payload: SignInPayload
        ): CompletableFuture<Any> {

                logger.info { "Sign-in request for personId: ${payload.personId}" }

                // 🛡️ The Identity Baton
                // We set 'x-user-id' to the personId.
                // This ensures the person 'signs themselves in'.
                val metadata =
                        MetaData.with("x-user-id", payload.personId.toString())
                                .and(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)

                val command = SignInCommand(personId = payload.personId)

                return commandGateway.send<Any>(command, metadata)
        }
}
