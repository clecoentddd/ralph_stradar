package stradar.organizationview.linkinitiativestoinitiative

import java.util.UUID
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

/** The "Answer" DTO */
data class InitiativeLinkDto(val id: UUID, val name: String)

@Component
class InitiativesLinkQueryHandler(private val repository: InitiativesLinkRepository) {

        @QueryHandler
        fun handle(query: GetLinkedInitiativesQuery): List<InitiativeLinkDto> {
                // Fetch entities from the database
                val entities =
                        repository.findAllByInitiativeIdAndOrganizationId(
                                query.initiativeId,
                                query.organizationId
                        )

                // The 'it' refers to each 'InitiativesLinkReadModel' in the list
                return entities.map { entity ->
                        InitiativeLinkDto(
                                id = entity.linkedInitiativeId,
                                name = entity.linkedInitiativeName
                        )
                }
        }
}
