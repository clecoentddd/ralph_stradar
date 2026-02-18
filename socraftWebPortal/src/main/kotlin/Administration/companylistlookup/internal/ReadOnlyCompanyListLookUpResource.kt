package administration.companylistlookup.internal

import administration.common.SettingsConstants
import administration.companylistlookup.CompanyListLookUpReadModelEntity
import administration.companylistlookup.CompanyListLookUpReadModelQuery
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822859
*/
@RestController
class CompanylistlookupResource(private val queryGateway: QueryGateway) {

     private val logger = KotlinLogging.logger {}

     @CrossOrigin
     @GetMapping("/companylistlookup") // Removed /{id} to match fixed settings logic
     fun findReadModel(): CompletableFuture<CompanyListLookUpReadModelEntity> {

          logger.info {
               "Fetching Company List for fixed Settings ID: ${SettingsConstants.SETTINGS_ID}"
          }

          return queryGateway.query(
                  CompanyListLookUpReadModelQuery(SettingsConstants.SETTINGS_ID),
                  CompanyListLookUpReadModelEntity::class.java
          )
     }
}
