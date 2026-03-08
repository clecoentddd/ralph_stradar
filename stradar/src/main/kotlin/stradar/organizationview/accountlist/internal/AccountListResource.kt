package stradar.organizationview.accountlist.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import stradar.organizationview.accountlist.AccountListReadModel
import stradar.organizationview.accountlist.AccountListReadModelEntity
import stradar.organizationview.accountlist.AccountListReadModelQuery
import stradar.organizationview.accountlist.PersonAccountQuery
import stradar.support.metadata.*

@RestController
class AccountListResource(private val queryGateway: QueryGateway) {

        private val logger = KotlinLogging.logger {}

        /** 1. Get the full list (Admin View) */
        @CrossOrigin(
                allowedHeaders =
                        [
                                ORGANIZATION_ID_HEADER,
                                SESSION_ID_HEADER,
                                "Content-Type",
                                "X-Correlation-Id",
                                USER_ID_HEADER]
        )
        @GetMapping("/accountlist")
        fun findReadModel(): CompletableFuture<ResponseEntity<AccountListReadModel>> {
                logger.info { "Fetching full account list" }
                return queryGateway.query(
                                AccountListReadModelQuery(),
                                AccountListReadModel::class.java
                        )
                        .thenApply { result -> ResponseEntity.ok(result) }
        }

        /** 2. Get specific person context (User Identity View) */
        @CrossOrigin
        @GetMapping("/account/{personId}")
        fun findPersonAccount(
                @PathVariable personId: UUID
        ): CompletableFuture<ResponseEntity<AccountListReadModelEntity>> {
                logger.info { "Fetching account details for personId: $personId" }

                // We use PersonAccountQuery which we added to the query handler
                return queryGateway.query(
                                PersonAccountQuery(personId),
                                ResponseTypes.instanceOf(AccountListReadModelEntity::class.java)
                        )
                        .thenApply { result ->
                                if (result != null) ResponseEntity.ok(result)
                                else ResponseEntity.notFound().build()
                        }
        }
}
