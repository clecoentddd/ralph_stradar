package administration.invoicestatemappinglookup.internal

import administration.common.SettingsConstants // Import the constant
import administration.invoicestatemappinglookup.InvoiceStateMappingLookUpReadModelEntity
import administration.invoicestatemappinglookup.InvoiceStateMappingLookUpReadModelQuery
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713541
*/
@RestController
class InvoicestatemappinglookupResource(private val queryGateway: QueryGateway) {

     private val logger = KotlinLogging.logger {}

     @CrossOrigin
     @GetMapping("/invoicestatemappinglookup")
     fun findReadModel(): CompletableFuture<InvoiceStateMappingLookUpReadModelEntity> {
          return queryGateway.query(
                  InvoiceStateMappingLookUpReadModelQuery(SettingsConstants.SETTINGS_ID),
                  InvoiceStateMappingLookUpReadModelEntity::class.java // Expect the entity back
          )
     }
}
