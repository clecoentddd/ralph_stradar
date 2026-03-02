package stradar.organizationview.strategies.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import stradar.organizationview.strategies.GetStrategiesByOrganizationQuery
import stradar.organizationview.strategies.GetStrategiesByTeamQuery
import stradar.organizationview.strategies.StrategiesReadModel

/*
Boardlink:
https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661684920684
*/

@RestController
@RequestMapping("/strategies")
class StrategiesResource(private val queryGateway: QueryGateway) {

     private val logger = KotlinLogging.logger {}

     /**
      * GET /strategies
      *
      * Required:
      * - organizationId OR teamId
      */
     @CrossOrigin
     @GetMapping
     fun findStrategies(
             @RequestParam(required = false) organizationId: UUID?,
             @RequestParam(required = false) teamId: UUID?
     ): CompletableFuture<StrategiesReadModel> {

          return when {
               organizationId != null -> {
                    queryGateway.query(
                            GetStrategiesByOrganizationQuery(organizationId),
                            StrategiesReadModel::class.java
                    )
               }
               teamId != null -> {
                    queryGateway.query(
                            GetStrategiesByTeamQuery(teamId),
                            StrategiesReadModel::class.java
                    )
               }
               else -> {
                    throw IllegalArgumentException(
                            "You must provide either organizationId or teamId"
                    )
               }
          }
     }
}
