package stradar.organizationview.environmentalchanges.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.organizationview.environmentalchanges.*
import stradar.organizationview.environmentalchanges.EnvironmentalChangesReadModel
import stradar.organizationview.environmentalchanges.EnvironmentalChangesReadModelQuery
import stradar.organizationview.environmentalchanges.EnvironmentalChangesTeamListQuery

@Component
class EnvironmentalChangesQueryHandler(
        private val repository: EnvironmentalChangesReadModelRepository
) {

  private val logger = KotlinLogging.logger {}

  /** 1. Handles the detail view: Hoists common data and maps to the DTO list */
  @QueryHandler
  fun handle(query: EnvironmentalChangesReadModelQuery): EnvironmentalChangesReadModel {
    logger.info { "Fetching elements for Environmental Change: ${query.environmentalChangeId}" }

    // Find all records associated with this change ID
    val entities = repository.findByEnvironmentalChangeId(query.environmentalChangeId)

    if (entities.isEmpty()) {
      return EnvironmentalChangesReadModel(
              environmentalChangeId = query.environmentalChangeId,
              teamId = UUID.randomUUID(),
              organizationId = UUID.randomUUID(),
              elements = emptyList()
      )
    }

    // Extract shared context from the first record (hoisting)
    val header = entities.first()

    return EnvironmentalChangesReadModel(
            environmentalChangeId = query.environmentalChangeId,
            teamId = header.teamId!!,
            organizationId = header.organizationId!!,
            elements =
                    entities.map { entity ->
                      EnvironmentalChangeElementDTO(
                              environmentalChangeId = entity.environmentalChangeId!!,
                              title = entity.title,
                              detect = entity.detect,
                              assess = entity.assess,
                              respond = entity.respond,
                              type = entity.type,
                              category = entity.category,
                              distance = entity.distance,
                              impact = entity.impact,
                              risk = entity.risk
                      )
                    }
    )
  }

  /** 2. Handles the team list view: Returns the flat entities directly */
  @QueryHandler
  fun handle(query: EnvironmentalChangesTeamListQuery): List<EnvironmentalChangesReadModelEntity> {
    logger.info { "Fetching flat list for Team: ${query.teamId}" }

    // This utilizes your repository's ability to filter by teamId
    return repository.findByTeamId(query.teamId)
  }
}
