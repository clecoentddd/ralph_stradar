package administration.client.projectlistlookup.internal

import administration.client.projectlistlookup.ListOfProjectsReadModelEntity
import administration.client.projectlistlookup.ListOfProjectsReadModelQuery
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660065978340
*/
@RestController
@RequestMapping("/client")
class ProjectlistlookupResource(
    private val queryGateway: QueryGateway // val is preferred for injected dependencies
) {

  private val logger = KotlinLogging.logger {}

  @CrossOrigin
  @GetMapping("/projectlistlookup/{id}")
  fun findReadModel(
      @PathVariable("id") companyId: Long
  ): CompletableFuture<ListOfProjectsReadModelEntity> {

    logger.info { "Handling request for ProjectList lookup: companyId $companyId" }

    // The second parameter tells Axon which QueryHandler to route to
    // based on its return type.
    return queryGateway.query(
        ListOfProjectsReadModelQuery(companyId), ListOfProjectsReadModelEntity::class.java)
  }
}
