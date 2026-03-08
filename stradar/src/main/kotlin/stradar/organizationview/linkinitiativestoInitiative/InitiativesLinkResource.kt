package stradar.organizationview.linkinitiativestoinitiative

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.web.bind.annotation.*
import stradar.support.metadata.*

@CrossOrigin
@RestController
@RequestMapping("/initiative-links") // Renamed to reflect Initiative-to-Initiative
@Tag(
        name = "Initiative Links",
        description = "Direct CRUD for linking Initiatives to other Initiatives"
)
class InitiativesLinkResource(private val repository: InitiativesLinkRepository) {

        @PostMapping("/{initiativeId}")
        @Operation(
                summary = "Update all links for an initiative",
                description =
                        "Deletes existing links and replaces them with a list of linked initiatives."
        )
        fun updateLinks(
                @PathVariable initiativeId: UUID,
                @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
                @RequestBody linksDto: List<InitiativeLinkDto>
        ) {
                // 1. Wipe old links to perform a clean "Sync"
                repository.deleteByInitiativeIdAndOrganizationId(initiativeId, organizationId)

                // 2. Map DTOs to the ReadModel Entities
                val entities =
                        linksDto.map { dto ->
                                InitiativesLinkReadModel(
                                        initiativeId = initiativeId,
                                        linkedInitiativeId = dto.id, // The target initiative
                                        linkedInitiativeName = dto.name, // The target name
                                        organizationId = organizationId
                                )
                        }
                repository.saveAll(entities)
        }

        @GetMapping("/{initiativeId}")
        @Operation(
                summary = "GetLinkedInitiatives",
                description = "Returns a list of IDs and Names of initiatives linked to this one."
        )
        fun getLinkedInitiatives(
                @PathVariable initiativeId: UUID,
                @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID
        ): List<InitiativeLinkDto> {
                return repository.findAllByInitiativeIdAndOrganizationId(
                                initiativeId,
                                organizationId
                        )
                        .map {
                                // Corrected mapping: Use the linked ID and Name from the entity
                                InitiativeLinkDto(it.linkedInitiativeId, it.linkedInitiativeName)
                        }
        }
}
