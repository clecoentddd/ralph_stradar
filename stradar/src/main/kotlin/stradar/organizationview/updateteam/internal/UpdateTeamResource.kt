package stradar.organizationview.updateteam.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand

data class UpdateTeamPayload(
        var teamId: UUID,
        var context: String,
        var level: Int,
        var name: String,
        var organizationId: UUID,
        var purpose: String
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631265233
*/
@RestController
class UpdateTeamResource(private var commandGateway: CommandGateway) {

    var logger = KotlinLogging.logger {}

    @CrossOrigin
    @PostMapping("/debug/updateteam")
    fun processDebugCommand(
            @RequestParam teamId: UUID,
            @RequestParam context: String,
            @RequestParam level: Int,
            @RequestParam name: String,
            @RequestParam organizationId: UUID,
            @RequestParam purpose: String
    ): CompletableFuture<Any> {
        return commandGateway.send(
                UpdateTeamCommand(teamId, context, level, name, organizationId, purpose)
        )
    }

    @CrossOrigin
    @PostMapping("/updateteam/{id}")
    fun processCommand(
            @PathVariable("id") teamId: UUID,
            @RequestBody payload: UpdateTeamPayload
    ): CompletableFuture<Any> {
        return commandGateway.send(
                UpdateTeamCommand(
                        teamId = payload.teamId,
                        context = payload.context,
                        level = payload.level,
                        name = payload.name,
                        organizationId = payload.organizationId,
                        purpose = payload.purpose
                )
        )
    }
}
