package administration.client.clientaccountlist.internal

import administration.client.clientaccountlist.ClientAccountListReadModel
import administration.client.clientaccountlist.ClientAccountListReadModelQuery
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660036569910
*/
@RestController
class ClientAccountListResource(private var queryGateway: QueryGateway) {

     var logger = KotlinLogging.logger {}

     @CrossOrigin
     @GetMapping("/clientaccountlist")
     fun findReadModel(): CompletableFuture<ClientAccountListReadModel> {
          return queryGateway.query(
                  ClientAccountListReadModelQuery(),
                  ClientAccountListReadModel::class.java
          )
     }
}
