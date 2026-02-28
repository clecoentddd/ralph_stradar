package stradar.organizationview.createteam.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import stradar.organizationview.domain.commands.createteam.CreateTeamCommand
import stradar.organizationview.teamlist.TeamNameUniquenessQuery
import stradar.support.metadata.SESSION_ID_HEADER

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

        @CrossOrigin
        @PostMapping("/createteam")
        fun processCommand(
                @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
                @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
                @RequestBody payload: CreateTeamPayload
        ): CompletableFuture<Any> {

                // 🔎 Master Skill: Uniqueness Validation
                val isDuplicate =
                        queryGateway
                                .query(
                                        TeamNameUniquenessQuery(
                                                payload.organizationId,
                                                payload.name
                                        ),
                                        Boolean::class.java
                                )
                                .get(5, TimeUnit.SECONDS)

                if (isDuplicate == true) {
                        throw IllegalArgumentException(
                                "Create Team - team name must be unique for a given organization"
                        )
                }

                val metadata =
                        MetaData.with(
                                        "X-Correlation-Id",
                                        correlationId ?: UUID.randomUUID().toString()
                                )
                                .and(SESSION_ID_HEADER, sessionId)

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
