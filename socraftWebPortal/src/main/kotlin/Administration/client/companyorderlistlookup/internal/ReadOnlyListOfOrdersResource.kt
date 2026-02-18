package administration.client.companyorderlistlookup.internal

import administration.client.companyorderlistlookup.ListOfOrdersReadModel
import administration.client.companyorderlistlookup.ListOfOrdersReadModelQuery
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660085256087
*/
@RestController
class CompanyorderlistlookupResource(private val queryGateway: QueryGateway) {

        private val logger = KotlinLogging.logger {}

        @CrossOrigin
        @GetMapping("/companyorderlistlookup/{id}")
        fun findReadModel(
                @PathVariable("id") companyId: Long
        ): CompletableFuture<ListOfOrdersReadModel> {

                logger.info { "Fetching order list for companyId: $companyId" }

                // Dispatches the query through the Axon QueryBus to the
                // ListOfOrdersReadModelQueryHandler
                return queryGateway.query(
                        ListOfOrdersReadModelQuery(companyId),
                        ListOfOrdersReadModel::class.java
                )
        }
}
