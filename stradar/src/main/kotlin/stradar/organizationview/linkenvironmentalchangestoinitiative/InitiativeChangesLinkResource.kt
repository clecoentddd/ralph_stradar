package stradar.organizationview.linkenvironmentalchangestoinitiative

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.web.bind.annotation.*
import stradar.support.metadata.*

@CrossOrigin(
    allowedHeaders =
        [
            ORGANIZATION_ID_HEADER,
            SESSION_ID_HEADER,
            "X-Correlation-Id",
            "Content-Type",
            USER_ID_HEADER])
@RestController
@RequestMapping("/env-links")
@Tag(
    name = "Initiative Environmental Links",
    description = "Direct CRUD for linking Env Changes to Initiatives")
class InitiativeChangesLinkResource(private val repository: InitiativeChangesLinkRepository) {

  @PostMapping("/{initiativeId}")
  @Operation(
      summary = "Update all links for an initiative",
      description = "Deletes existing links and replaces them with the provided list.")
  fun updateLinks(
      @PathVariable initiativeId: UUID,
      @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
      @RequestBody linksDto: List<EnvLinkDto>
  ) {
    repository.deleteByInitiativeIdAndOrganizationId(initiativeId, organizationId)

    val entities =
        linksDto.map { dto ->
          InitiativeChangesLinkReadModel(
              initiativeId = initiativeId,
              envChangeId = dto.id,
              envChangeName = dto.name,
              organizationId = organizationId)
        }
    repository.saveAll(entities)
  }

  @GetMapping("/{initiativeId}")
  @Operation(
      summary = "GetEnvChangesLinksToInitiative",
      description = "Returns a list of linked IDs and Names.")
  fun getEnvChangesLinksToInitiative(
      @PathVariable initiativeId: UUID,
      @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID
  ): List<EnvLinkDto> {
    return repository.findAllByInitiativeIdAndOrganizationId(initiativeId, organizationId).map {
      EnvLinkDto(it.envChangeId, it.envChangeName)
    }
  }

  data class EnvLinkDto(val id: UUID, val name: String)
}
