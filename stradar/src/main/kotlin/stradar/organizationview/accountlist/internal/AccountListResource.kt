package stradar.organizationview.accountlist.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import stradar.organizationview.accountlist.AccountListReadModel
import stradar.organizationview.accountlist.AccountListReadModelEntity
import stradar.organizationview.accountlist.AccountListReadModelQuery
import stradar.organizationview.accountlist.PersonAccountQuery
import stradar.security.SecurityHelper
import stradar.support.metadata.*

@RestController
class AccountListResource(
        private val queryGateway: QueryGateway,
        private val securityHelper: SecurityHelper
) {

  private val logger = KotlinLogging.logger {}
  private val COMPANY_ID_CLAIM = "https://stradar.com/companyId"

  /** 1. Get the full list (Admin View) */
  @CrossOrigin(
          allowedHeaders =
                  [
                          "Authorization",
                          ORGANIZATION_ID_HEADER,
                          SESSION_ID_HEADER,
                          "Content-Type",
                          "X-Correlation-Id",
                          USER_ID_HEADER]
  )
  @GetMapping("/accountlist")
  fun findReadModel(): CompletableFuture<ResponseEntity<AccountListReadModel>> {
    logger.info { "Fetching full account list" }
    return queryGateway.query(AccountListReadModelQuery(), AccountListReadModel::class.java)
            .thenApply { result -> ResponseEntity.ok(result) }
  }

  /** 2. Get specific person context (User Identity View) */
  @CrossOrigin(
          allowedHeaders =
                  [
                          "Authorization",
                          ORGANIZATION_ID_HEADER,
                          SESSION_ID_HEADER,
                          "Content-Type",
                          "X-Correlation-Id",
                          USER_ID_HEADER]
  )
  @GetMapping("/account/{personId}")
  fun findPersonAccount(
          @PathVariable personId: UUID,
          authentication: Authentication
  ): CompletableFuture<ResponseEntity<AccountListReadModelEntity>> {

    val user = securityHelper.extractUser(authentication)
    logger.info {
      "Fetching account for personId=$personId by auth0User=${user.auth0UserId} companyId=${user.companyId}"
    }

    return queryGateway.query(
                    PersonAccountQuery(personId),
                    ResponseTypes.instanceOf(AccountListReadModelEntity::class.java)
            )
            .thenApply { result ->
              if (result == null)
                      return@thenApply ResponseEntity.notFound().build<AccountListReadModelEntity>()

              // Verify the requesting user owns this account
              securityHelper.checkSameUser<AccountListReadModelEntity>(user, result.auth0UserId)
                      ?.let {
                        return@thenApply it
                      }

              // Verify the account belongs to the correct organization
              securityHelper.checkOrganization<AccountListReadModelEntity>(
                              user,
                              result.organizationId
                      )
                      ?.let {
                        return@thenApply it
                      }

              ResponseEntity.ok(result)
            }
  }
}
