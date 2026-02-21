package administration.admin.companylistlookup.internal

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity
import administration.admin.companylistlookup.FetchAllCompaniesQuery
import administration.admin.companylistlookup.FetchCompanyNameByCompanyIdQuery
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class CompanylistlookupResource(private val queryGateway: QueryGateway) {

        private val logger = KotlinLogging.logger {}

        @CrossOrigin(origins = ["\${app.frontend-url:http://localhost:8081}"])
        @GetMapping("/companylistlookup/all")
        fun findAllCompanies(
                @RequestHeader("X-Session-Id") sessionId: String
        ): CompletableFuture<List<CompanyListLookUpReadModelEntity>> {

                logger.info { "Admin Session $sessionId fetching all companies" }

                // multipleInstancesOf is required when returning a List
                return queryGateway.query(
                        FetchAllCompaniesQuery(),
                        ResponseTypes.multipleInstancesOf(
                                CompanyListLookUpReadModelEntity::class.java
                        )
                )
        }

        @CrossOrigin(origins = ["\${app.frontend-url:http://localhost:8081}"])
        @GetMapping("/companyname/{companyId}")
        fun findCompanyName(
                @PathVariable companyId: Long,
                @RequestHeader("X-Session-Id") sessionId: String
        ): CompletableFuture<String> {

                logger.info { "Session $sessionId fetching name for company: $companyId" }

                // instanceOf is used for single objects or primitives (String)
                return queryGateway.query(
                        FetchCompanyNameByCompanyIdQuery(companyId),
                        ResponseTypes.instanceOf(String::class.java)
                )
        }
}
