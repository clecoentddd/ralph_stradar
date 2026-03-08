package stradar.organizationview.createteam.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import stradar.common.queryWithMetaData
import stradar.organizationview.domain.commands.createteam.CreateTeamCommand
import stradar.organizationview.teamlist.TeamNameAlreadyExistsQuery
import stradar.support.metadata.*

data class CreateTeamPayload(
        var organizationId: UUID,
        var adminAccountId: UUID,
        var organizationName: String,
        var context: String,
        var level: Int,
        var name: String,
        var purpose: String
)

@RestController
class CreateTeamResource(
        private val commandGateway: CommandGateway,
        private val queryGateway: QueryGateway
) {

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
        @PostMapping("/createteam")
        fun processCommand(
                @RequestHeader(USER_ID_HEADER) userId: String,
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestBody payload: CreateTeamPayload
        ): CompletableFuture<Any> {

                logger.info { "Create Team - payload: $payload" }

                val metadata =
                        MetaData.with(USER_ID_HEADER, userId)
                                .and(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)
                                .and(ORGANIZATION_ID_HEADER, payload.organizationId)

                if (payload.level < 0) {
                        throw IllegalArgumentException(
                                "Invalid team level: $payload.level. Levels must be non-negative."
                        )
                }
                val nameAlreadyExists =
                        queryGateway
                                .queryWithMetaData(
                                        TeamNameAlreadyExistsQuery(
                                                payload.organizationId,
                                                payload.name
                                        ),
                                        metadata,
                                        ResponseTypes.instanceOf(Boolean::class.java)
                                )
                                .get(5, TimeUnit.SECONDS)

                // If it ALREADY EXISTS (True), then we throw the error
                if (nameAlreadyExists == true) {
                        throw IllegalArgumentException("Create Team - team name must be unique")
                }
                return commandGateway.send(
                        CreateTeamCommand(
                                teamId = UUID.randomUUID(),
                                organizationId = payload.organizationId,
                                context = payload.context,
                                level = payload.level,
                                name = payload.name,
                                purpose = payload.purpose
                        ),
                        metadata
                )
        }
}
