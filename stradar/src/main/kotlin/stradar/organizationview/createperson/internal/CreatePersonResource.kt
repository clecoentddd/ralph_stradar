package stradar.organizationview.createperson.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.createperson.CreatePersonCommand
import stradar.support.metadata.*

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

        @CrossOrigin(
                allowedHeaders =
                        [
                                ORGANIZATION_ID_HEADER,
                                SESSION_ID_HEADER,
                                "Content-Type",
                                "X-Correlation-Id",
                                USER_ID_HEADER]
        )
        @PostMapping("/debug/createperson")
        fun processDebugCommand(
                @RequestHeader(
                        value = USER_ID_HEADER,
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

                val metadata =
                        MetaData.with(USER_ID_HEADER, userId)
                                .and(ORGANIZATION_ID_HEADER, organizationId)
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
                        value = USER_ID_HEADER,
                        required = false,
                        defaultValue = "\${user.name}"
                )
                userId: String,
                @PathVariable("id") personId: UUID,
                @RequestBody payload: CreatePersonPayload
        ): CompletableFuture<Any> {

                // 🛡️ Ensure the 'who' is passed to the 'what'
                val metadata =
                        MetaData.with(USER_ID_HEADER, userId)
                                .and(ORGANIZATION_ID_HEADER, payload.organizationId)
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
