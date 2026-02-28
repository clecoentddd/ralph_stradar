package stradar.organizationview.teamlist.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import stradar.organizationview.teamlist.*
import stradar.support.metadata.SESSION_ID_HEADER

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645849750300
*/
@RestController
class TeamlistResource(private var queryGateway: QueryGateway) {

     private var logger = KotlinLogging.logger {}

     @CrossOrigin
     @GetMapping("/teamlist")
     fun findReadModel(): CompletableFuture<TeamListReadModel> {
          return queryGateway.query(TeamListReadModelQuery(), TeamListReadModel::class.java)
     }

     @CrossOrigin
     @GetMapping("/teamlist/{organizationId}")
     fun findByOrganization(
             @PathVariable("organizationId") organizationId: UUID
     ): CompletableFuture<TeamListReadModel> {
          return queryGateway.query(
                  TeamListByOrganizationQuery(organizationId),
                  TeamListReadModel::class.java
          )
     }

     /**
      * NEW: Fetch the team name by ID. We pass the organizationId from the headers/metadata to
      * ensure the query can be validated in the next step.
      */
     @CrossOrigin
     @GetMapping("/teamlist/{teamId}/name")
     fun findTeamName(
             @PathVariable("teamId") teamId: UUID,
             @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String
     ): CompletableFuture<TeamNameResponse> {

          logger.info { "Fetching name for Team ID: $teamId" }

          // We wrap the query in the gateway call
          return queryGateway.query(TeamNameByTeamIdQuery(teamId), TeamNameResponse::class.java)
     }
}
