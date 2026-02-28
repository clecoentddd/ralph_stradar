package stradar.organizationview.environmentalchanges.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import stradar.organizationview.environmentalchanges.*
import stradar.organizationview.environmentalchanges.EnvironmentalChangesReadModel
import stradar.organizationview.environmentalchanges.EnvironmentalChangesReadModelEntity
import stradar.organizationview.environmentalchanges.EnvironmentalChangesReadModelQuery
import stradar.organizationview.environmentalchanges.EnvironmentalChangesTeamListQuery

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661040894563
*/
@RestController
class EnvironmentalChangesResource(private var queryGateway: QueryGateway) {

        private val logger = KotlinLogging.logger {}

        /** 1. Fetch a specific Environmental Change View (Hoisted DTO) */
        @CrossOrigin
        @GetMapping("/environmentalchanges/{environmentalChangeId}")
        fun findReadModel(
                @PathVariable("environmentalChangeId") environmentalChangeId: UUID
        ): CompletableFuture<EnvironmentalChangesReadModel> {
                logger.info { "API Request: Fetching View for $environmentalChangeId" }

                return queryGateway.query(
                        EnvironmentalChangesReadModelQuery(environmentalChangeId),
                        EnvironmentalChangesReadModel::class.java
                )
        }

        /** 2. Fetch all Environmental Changes for a Team (Flat List) */
        @CrossOrigin
        @GetMapping("/environmentalchanges/team/{teamId}")
        fun findByTeam(
                @PathVariable("teamId") teamId: UUID
        ): CompletableFuture<List<EnvironmentalChangesReadModelEntity>> {
                logger.info { "API Request: Fetching all changes for Team: $teamId" }

                return queryGateway.query(
                        EnvironmentalChangesTeamListQuery(teamId),
                        ResponseTypes.multipleInstancesOf(
                                EnvironmentalChangesReadModelEntity::class.java
                        )
                )
        }
}
