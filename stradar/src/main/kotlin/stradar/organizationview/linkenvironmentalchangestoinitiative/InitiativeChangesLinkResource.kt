package stradar.organizationview.linkenvironmentalchangestoinitiative

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import stradar.security.SecurityHelper
import stradar.support.metadata.*

@RestController
@RequestMapping("/env-links")
@Tag(
        name = "Initiative Environmental Links",
        description = "Direct CRUD for linking Env Changes to Initiatives"
)
class InitiativeChangesLinkResource(
        private val repository: InitiativeChangesLinkRepository,
        private val securityHelper: SecurityHelper
) {

  val logger = KotlinLogging.logger {}

  @CrossOrigin(
          allowedHeaders =
                  [
                          "Authorization",
                          ORGANIZATION_ID_HEADER,
                          SESSION_ID_HEADER,
                          "X-Correlation-Id",
                          "Content-Type",
                          USER_ID_HEADER]
  )
  @PostMapping("/{initiativeId}")
  @Operation(
          summary = "Update all links for an initiative",
          description = "Deletes existing links and replaces them with the provided list."
  )
  fun updateLinks(
          @PathVariable initiativeId: UUID,
          @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
          @RequestBody linksDto: List<EnvLinkDto>,
          authentication: Authentication
  ): ResponseEntity<Any> {

    // 🔒 Verify user belongs to the organization
    val user = securityHelper.extractUser(authentication)
    securityHelper.checkOrganization<Any>(user, organizationId)?.let {
      return it
    }

    repository.deleteByInitiativeIdAndOrganizationId(initiativeId, organizationId)

    val entities =
            linksDto.map { dto ->
              InitiativeChangesLinkReadModel(
                      initiativeId = initiativeId,
                      envChangeId = dto.id,
                      envChangeName = dto.name,
                      organizationId = organizationId
              )
            }
    repository.saveAll(entities)

    return ResponseEntity.ok().build()
  }

  @CrossOrigin(
          allowedHeaders =
                  [
                          "Authorization",
                          ORGANIZATION_ID_HEADER,
                          SESSION_ID_HEADER,
                          "X-Correlation-Id",
                          "Content-Type",
                          USER_ID_HEADER]
  )
  @GetMapping("/{initiativeId}")
  @Operation(
          summary = "GetEnvChangesLinksToInitiative",
          description = "Returns a list of linked IDs and Names."
  )
  fun getEnvChangesLinksToInitiative(
          @PathVariable initiativeId: UUID,
          @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
          authentication: Authentication
  ): ResponseEntity<List<EnvLinkDto>> {
    logger.info {
      "Getting env change links for initiative $initiativeId in organization $organizationId"
    }

    // 🔒 Verify user belongs to the organization
    val user = securityHelper.extractUser(authentication)
    securityHelper.checkOrganization<List<EnvLinkDto>>(user, organizationId)?.let {
      return it
    }

    val result =
            repository.findAllByInitiativeIdAndOrganizationId(initiativeId, organizationId).map {
              EnvLinkDto(it.envChangeId, it.envChangeName)
            }

    return ResponseEntity.ok(result)
  }

  data class EnvLinkDto(val id: UUID, val name: String)
}
