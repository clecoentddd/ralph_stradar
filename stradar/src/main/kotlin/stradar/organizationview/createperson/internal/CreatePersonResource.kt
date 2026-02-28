package stradar.organizationview.createperson.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.createperson.CreatePersonCommand

data class CreatePersonPayload(
        val personId: UUID,
        val organizationId: UUID,
        val organizationName: String,
        val role: String,
        val username: String
)

@RestController
class CreatePersonResource(private val commandGateway: CommandGateway) {

        private val logger = KotlinLogging.logger {}

        @CrossOrigin
        @PostMapping("/debug/createperson")
        fun processDebugCommand(
                @RequestHeader(
                        value = "X-User-Id",
                        required = false,
                        defaultValue = "\${user.name}"
                )
                userId: String,
                @RequestParam personId: UUID,
                @RequestParam organizationId: UUID,
                @RequestParam organizationName: String,
                @RequestParam role: String,
                @RequestParam username: String
        ): CompletableFuture<Any> {

                // 🛡️ Create the metadata baton
                val metadata =
                        MetaData.with("x-user-id", userId)
                                .and("X-Correlation-Id", UUID.randomUUID().toString())

                return commandGateway.send(
                        CreatePersonCommand(
                                personId = personId,
                                organizationId = organizationId,
                                organizationName = organizationName,
                                role = role,
                                username = username
                        ),
                        metadata
                )
        }

        @CrossOrigin
        @PostMapping("/createperson/{id}")
        fun processCommand(
                @RequestHeader(
                        value = "X-User-Id",
                        required = false,
                        defaultValue = "\${user.name}"
                )
                userId: String,
                @PathVariable("id") personId: UUID,
                @RequestBody payload: CreatePersonPayload
        ): CompletableFuture<Any> {

                // 🛡️ Ensure the 'who' is passed to the 'what'
                val metadata =
                        MetaData.with("x-user-id", userId)
                                .and("X-Correlation-Id", UUID.randomUUID().toString())

                return commandGateway.send(
                        CreatePersonCommand(
                                personId = personId,
                                organizationId = payload.organizationId,
                                organizationName = payload.organizationName,
                                role = payload.role,
                                username = payload.username
                        ),
                        metadata
                )
        }
}
