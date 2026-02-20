package administration.client.fetchprojects.internal

import administration.client.domain.commands.fetchprojects.MarkListOfProjectsFetchedCommand
import administration.common.ProjectDetails
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.collections.List
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class fetchprojectsPayload(
        var clientId: UUID,
        var companyId: Long,
        var projectList: List<ProjectDetails>
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660044053423
*/
@RestController
@RequestMapping("/client")
class MarkListOfProjectsFetchedResource(private var commandGateway: CommandGateway) {

  var logger = KotlinLogging.logger {}

  @CrossOrigin
  @PostMapping("/debug/fetchprojects")
  fun processDebugCommand(
          @RequestParam clientId: UUID,
          @RequestParam companyId: Long,
          @RequestParam projectList: List<ProjectDetails>
  ): CompletableFuture<Any> {
    return commandGateway.send(MarkListOfProjectsFetchedCommand(clientId, companyId, projectList))
  }

  @CrossOrigin
  @PostMapping("/fetchprojects/{id}")
  fun processCommand(
          @PathVariable("id") companyId: Long,
          @RequestBody payload: fetchprojectsPayload
  ): CompletableFuture<Any> {
    return commandGateway.send(
            MarkListOfProjectsFetchedCommand(
                    clientId = payload.clientId,
                    companyId = payload.companyId,
                    projectList = payload.projectList
            )
    )
  }
}
