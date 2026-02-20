package administration.admin.companylistlookup.internal

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity
import administration.admin.companylistlookup.CompanyListLookUpReadModelQuery
import administration.common.SettingsConstants
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822859
*/
@RestController
@RequestMapping("/admin")
class CompanylistlookupResource(private val queryGateway: QueryGateway) {

  private val logger = KotlinLogging.logger {}

  @CrossOrigin(origins = ["\${app.frontend-url:http://localhost:8081}"])
  @GetMapping("/companylistlookup")
  fun findReadModel(
          // Add this to satisfy the "Missing required header" check
          @RequestHeader("X-Session-Id") sessionId: String
  ): CompletableFuture<CompanyListLookUpReadModelEntity> {

    logger.info {
      "Fetching Company List for Session: $sessionId using fixed Settings ID: ${SettingsConstants.SETTINGS_ID}"
    }

    return queryGateway.query(
            CompanyListLookUpReadModelQuery(SettingsConstants.SETTINGS_ID),
            CompanyListLookUpReadModelEntity::class.java
    )
  }
}
