package stradar.organizationview.signin.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.springframework.web.bind.annotation.*
import stradar.organizationview.accountlist.internal.AccountListReadModelRepository
import stradar.organizationview.domain.commands.signin.SignInCommand
import stradar.support.metadata.*

data class SignInRequest(val usernameOrEmail: String, val password: String)

@RestController
class SignInResource(
        private val commandGateway: CommandGateway,
        private val accountListReadModelRepository: AccountListReadModelRepository,
        private val auth0Service: OrganizationAuth0Service // local module Auth0 adapter
) {

  private val logger = KotlinLogging.logger {}
  private val COMPANY_ID_CLAIM = "https://stradar.com/companyId"

  @CrossOrigin(
          allowedHeaders =
                  [
                          ORGANIZATION_ID_HEADER,
                          SESSION_ID_HEADER,
                          "Content-Type",
                          "X-Correlation-Id",
                          USER_ID_HEADER]
  )
  @PostMapping(value = ["/signin", "/signintoorganizationpersonaccount"])
  fun processCommand(
          @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String?,
          @RequestHeader(value = SESSION_ID_HEADER, required = true) sessionId: String,
          @RequestBody request: SignInRequest
  ): CompletableFuture<Any> {

    // 1️⃣ Authenticate via Auth0 using username/email + password
    val tokens = auth0Service.loginUser(request.usernameOrEmail, request.password)

    // 2️⃣ Decode id_token for user identity claims (email, nickname, subject)
    val idJwt = auth0Service.decodeJwt(tokens.idToken)
    val auth0UserId = idJwt.subject ?: throw IllegalArgumentException("JWT missing subject")
    val email =
            idJwt.getClaimAsString("email") ?: throw IllegalArgumentException("JWT missing email")
    val username = idJwt.getClaimAsString("nickname") ?: email

    // 3️⃣ Decode access_token for custom claims (companyId added via Auth0 Action)
    val accessJwt = auth0Service.decodeJwt(tokens.accessToken)
    val jwtCompanyId =
            accessJwt.getClaimAsString(COMPANY_ID_CLAIM)
                    ?: throw IllegalArgumentException("JWT missing companyId")
    val organizationUuid =
            try {
              UUID.fromString(jwtCompanyId)
            } catch (ex: IllegalArgumentException) {
              throw IllegalArgumentException("Invalid organizationId format")
            }

    // 4️⃣ Resolve person in database by auth0UserId
    val person =
            accountListReadModelRepository.findByAuth0UserId(auth0UserId)
                    ?: throw IllegalArgumentException(
                            "No person found for auth0UserId=$auth0UserId"
                    )
    val personId = person.personId ?: throw IllegalStateException("Person record has null personId")

    logger.info {
      "Resolved personId=$personId for auth0UserId=$auth0UserId, username=$username, email=$email"
    }

    // 5️⃣ Build metadata for Axon
    val metadata =
            MetaData.with(USER_ID_HEADER, personId.toString())
                    .and("X-Correlation-Id", correlationId ?: UUID.randomUUID().toString())
                    .and(SESSION_ID_HEADER, sessionId)
                    .and(ORGANIZATION_ID_HEADER, organizationUuid)
                    .and("email", email)
                    .and("username", username)

    // 6️⃣ Dispatch sign-in command
    val command = SignInCommand(personId = personId)
    val result = commandGateway.send<Any>(command, metadata)

    // 7️⃣ Return access token and basic user info for frontend
    return result.thenApply {
      mapOf(
              "token" to tokens.accessToken,
              "personId" to personId,
              "email" to email,
              "username" to username,
              "organizationId" to organizationUuid
      )
    }
  }
}
