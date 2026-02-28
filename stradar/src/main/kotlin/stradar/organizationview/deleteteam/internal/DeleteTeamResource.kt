package stradar.organizationview.deleteteam.internal

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand

import java.util.UUID
import java.util.concurrent.CompletableFuture


data class DeleteTeamPayload(	var teamId:UUID,
	var organizationId:UUID)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631612141
*/
@RestController
class DeleteTeamResource(private var commandGateway: CommandGateway) {

    var logger = KotlinLogging.logger {}

    
    @CrossOrigin
    @PostMapping("/debug/deleteteam")
    fun processDebugCommand(@RequestParam teamId:UUID,
	@RequestParam organizationId:UUID):CompletableFuture<Any> {
        return commandGateway.send(DeleteTeamCommand(teamId,
	organizationId))
    }
    

    
       @CrossOrigin
       @PostMapping("/deleteteam/{id}")
    fun processCommand(
        @PathVariable("id") teamId: UUID,
        @RequestBody payload: DeleteTeamPayload
    ):CompletableFuture<Any> {
         return commandGateway.send(DeleteTeamCommand(			teamId=payload.teamId,
			organizationId=payload.organizationId))
        }
       

}
