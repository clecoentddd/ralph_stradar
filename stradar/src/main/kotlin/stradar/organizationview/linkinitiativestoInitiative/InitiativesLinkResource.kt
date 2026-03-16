package stradar.organizationview.linkinitiativestoinitiative

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
@RequestMapping("/initiative-links")
@Tag(
        name = "Initiative Links",
        description = "Direct CRUD for linking Initiatives to other Initiatives"
)
class InitiativesLinkResource(
        private val repository: InitiativesLinkRepository,
        private val securityHelper: SecurityHelper
) {

  val logger = KotlinLogging.logger {}

  @CrossOrigin(
          allowedHeaders =
                  [
                          "Authorization",
                          ORGANIZATION_ID_HEADER,
                          SESSION_ID_HEADER,
                          "Content-Type",
                          "X-Correlation-Id",
                          USER_ID_HEADER]
  )
  @PostMapping("/{initiativeId}")
  @Operation(
          summary = "Update all links for an initiative",
          description =
                  "Deletes existing links and replaces them with a list of linked initiatives."
  )
  fun updateLinks(
          @PathVariable initiativeId: UUID,
          @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
          @RequestBody linksDto: List<InitiativeLinkDto>,
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
              InitiativesLinkReadModel(
                      initiativeId = initiativeId,
                      linkedInitiativeId = dto.id,
                      linkedInitiativeName = dto.name,
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
                          "Content-Type",
                          "X-Correlation-Id",
                          USER_ID_HEADER]
  )
  @GetMapping("/{initiativeId}")
  @Operation(
          summary = "GetLinkedInitiatives",
          description = "Returns a list of IDs and Names of initiatives linked to this one."
  )
  fun getLinkedInitiatives(
          @PathVariable initiativeId: UUID,
          @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
          authentication: Authentication
  ): ResponseEntity<List<InitiativeLinkDto>> {
    logger.info {
      "Getting linked initiatives for initiative $initiativeId in organization $organizationId"
    }

    // 🔒 Verify user belongs to the organization
    val user = securityHelper.extractUser(authentication)
    securityHelper.checkOrganization<List<InitiativeLinkDto>>(user, organizationId)?.let {
      return it
    }

    val result =
            repository.findAllByInitiativeIdAndOrganizationId(initiativeId, organizationId).map {
              InitiativeLinkDto(it.linkedInitiativeId, it.linkedInitiativeName)
            }

    return ResponseEntity.ok(result)
  }
}
