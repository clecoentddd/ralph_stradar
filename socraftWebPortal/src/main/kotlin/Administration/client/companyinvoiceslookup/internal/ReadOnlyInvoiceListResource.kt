package administration.client.companyinvoiceslookup.internal

import administration.client.companyinvoiceslookup.InvoiceListReadModel
import administration.client.companyinvoiceslookup.InvoiceListReadModelQuery
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660086962398
*/
@RestController
class CompanyinvoiceslookupResource(private val queryGateway: QueryGateway) {

        private val logger = KotlinLogging.logger {}

        @CrossOrigin
        @GetMapping("/companyinvoiceslookup/{id}")
        fun findReadModel(
                @PathVariable("id") companyId: Long
        ): CompletableFuture<InvoiceListReadModel> {

                logger.info { "Request received to fetch invoices for company: $companyId" }

                // Dispatches the query through the Axon Query Bus
                return queryGateway.query(
                        InvoiceListReadModelQuery(companyId),
                        InvoiceListReadModel::class.java
                )
        }
}
