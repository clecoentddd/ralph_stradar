package stradar.platformadministration.organizationlist.internal

import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import stradar.platformadministration.organizationlist.OrganizationListReadModel
import stradar.platformadministration.organizationlist.OrganizationListReadModelQuery

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645830935933
*/
@RestController
class OrganizationListResource(private val queryGateway: QueryGateway) {

  private val logger = KotlinLogging.logger {}

  @CrossOrigin
  @GetMapping("/organizationlist")
  fun findReadModel(): CompletableFuture<ResponseEntity<OrganizationListReadModel>> {
    logger.info { "Fetching organization list" }
    return queryGateway
        .query(OrganizationListReadModelQuery(), OrganizationListReadModel::class.java)
        .thenApply { result -> ResponseEntity.ok(result) }
  }
}
