package stradar.platformadministration.superadminaccount.internal

import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import stradar.platformadministration.superadminaccount.SuperAdminAccountReadModel
import stradar.platformadministration.superadminaccount.SuperAdminAccountReadModelQuery

@RestController
class SuperAdminAccountResource(private val queryGateway: QueryGateway) {

  private val logger = KotlinLogging.logger {}

  @CrossOrigin
  @GetMapping("/superadminaccount")
  fun findReadModel(): CompletableFuture<ResponseEntity<SuperAdminAccountReadModel>> {

    logger.info { "Simplified fetch for superadmin account details" }

    // Signature: query(Payload, ResponseClass)
    // This is the cleanest, most reliable way in Kotlin/Axon
    return queryGateway.query(
                    SuperAdminAccountReadModelQuery(),
                    SuperAdminAccountReadModel::class.java
            )
            .thenApply { result ->
              if (result != null) {
                ResponseEntity.ok(result)
              } else {
                ResponseEntity.notFound().build()
              }
            }
  }
}
