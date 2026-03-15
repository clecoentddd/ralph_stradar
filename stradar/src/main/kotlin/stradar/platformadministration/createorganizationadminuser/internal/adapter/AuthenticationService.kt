package stradar.platformadministration.createorganizationadminuser.internal.adapter

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class AuthenticationService(
        private val webClient: WebClient.Builder,
        @Value("\${auth0.domain}") private val domain: String,
        @Value("\${auth0.managementToken}") private val managementToken: String,
        @Value("\${auth0.devPassword:#{null}}") private val devPassword: String? = null
) {

  private val logger = KotlinLogging.logger {}

  init {
    logger.info { "Auth0 config — domain: $domain, token prefix: ${managementToken.take(20)}..." }
  }

  fun createUser(email: String, companyId: String): String {
    logger.info { "Creating Auth0 user for email: $email with companyId: $companyId" }

    require(email.isNotBlank()) { "Authentication Setup: Email must not be blank" }
    require(companyId.isNotBlank()) { "Authentication Setup: companyId must not be blank" }

    val request =
            Auth0CreateUserRequest(
                    email = email,
                    password = generateSecurePassword(),
                    app_metadata = mapOf("companyId" to companyId)
            )

    val response =
            webClient
                    .build()
                    .post()
                    .uri("https://$domain/api/v2/users")
                    .header("Authorization", "Bearer $managementToken")
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus({ status -> status.isError }) { clientResponse ->
                      clientResponse.bodyToMono(String::class.java).flatMap { errorBody ->
                        logger.error {
                          "Auth0 user creation failed — status: ${clientResponse.statusCode()}, body: $errorBody"
                        }
                        Mono.error(
                                RuntimeException(
                                        "Auth0 returned ${clientResponse.statusCode()} for email=$email: $errorBody"
                                )
                        )
                      }
                    }
                    .bodyToMono(Auth0UserResponse::class.java)
                    .doOnError { ex ->
                      logger.error(ex) {
                        "WebClient error while creating Auth0 user for email: $email"
                      }
                    }
                    .block()

    val userId =
            response?.user_id
                    ?: throw RuntimeException("Auth0 returned 2xx but no user_id for email=$email")

    logger.info { "Auth0 user created successfully — userId: $userId, email: $email" }
    return userId
  }

  fun sendPasswordSetupEmail(auth0UserId: String) {
    logger.info { "Sending password setup email for user: $auth0UserId" }

    val request = Auth0PasswordTicketRequest(user_id = auth0UserId)

    webClient
            .build()
            .post()
            .uri("https://$domain/api/v2/tickets/password-change")
            .header("Authorization", "Bearer $managementToken")
            .header("Content-Type", "application/json")
            .bodyValue(request)
            .retrieve()
            .onStatus({ status -> status.isError }) { clientResponse ->
              clientResponse.bodyToMono(String::class.java).flatMap { errorBody ->
                logger.error {
                  "Auth0 send password email failed — status: ${clientResponse.statusCode()}, body: $errorBody"
                }
                Mono.error(
                        RuntimeException(
                                "Auth0 returned ${clientResponse.statusCode()} for userId=$auth0UserId: $errorBody"
                        )
                )
              }
            }
            .bodyToMono(Void::class.java)
            .block()

    logger.info { "Password setup email triggered for user: $auth0UserId" }
  }

  private fun generateSecurePassword(): String {

    if (devPassword != null) {
      logger.warn { "Using fixed dev password — never use in production! $devPassword" }
      return devPassword
    }

    val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lower = "abcdefghijklmnopqrstuvwxyz"
    val digits = "0123456789"
    val special = "!@#\$%^&*"
    val all = upper + lower + digits + special

    // Guarantee at least one of each required character class
    val password =
            listOf(upper.random(), lower.random(), digits.random(), special.random()) +
                    (1..12).map { all.random() }

    return password.shuffled().joinToString("")
  }
}

data class Auth0CreateUserRequest(
        val email: String,
        val password: String,
        val connection: String = "Username-Password-Authentication",
        val email_verified: Boolean = false,
        val app_metadata: Map<String, String>
)

data class Auth0UserResponse(val user_id: String)

data class Auth0PasswordTicketRequest(val user_id: String)
