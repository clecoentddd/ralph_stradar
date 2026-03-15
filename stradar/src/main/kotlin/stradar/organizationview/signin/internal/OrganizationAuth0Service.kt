package stradar.organizationview.signin.internal

import com.nimbusds.jwt.JWTParser
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

data class Auth0TokenResponse(val accessToken: String, val idToken: String)

@Component
class OrganizationAuth0Service(
        private val webClient: WebClient.Builder,
        @Value("\${auth0.domain}") private val domain: String,
        @Value("\${auth0.clientId:}") private val clientId: String?,
        @Value("\${auth0.clientSecret:}") private val clientSecret: String?
) {

    private val logger = KotlinLogging.logger {}

    fun loginUser(usernameOrEmail: String, password: String): Auth0TokenResponse {
        logger.info { "Authenticating user $usernameOrEmail via Auth0" }
        logger.info { "Auth0 clientId=$clientId domain=$domain" }

        val formBody =
                "grant_type=password" +
                        "&username=$usernameOrEmail" +
                        "&password=$password" +
                        "&client_id=$clientId" +
                        "&client_secret=$clientSecret" +
                        "&realm=Username-Password-Authentication" +
                        "&scope=openid profile email" +
                        "&audience=https://$domain/api/v2/"

        val response =
                webClient
                        .build()
                        .post()
                        .uri("https://$domain/oauth/token")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .bodyValue(formBody)
                        .retrieve()
                        .onStatus({ it.isError }) { clientResponse ->
                            clientResponse.bodyToMono(String::class.java).flatMap { errorBody ->
                                logger.error {
                                    "Auth0 error response (${clientResponse.statusCode()}): $errorBody"
                                }
                                Mono.error(RuntimeException("Auth0 login failed: $errorBody"))
                            }
                        }
                        .bodyToMono(Map::class.java)
                        .block()
                        ?: throw RuntimeException("Auth0 login failed: no response")

        val accessToken =
                response["access_token"] as? String
                        ?: throw RuntimeException("Auth0 login failed: missing access_token")
        val idToken =
                response["id_token"] as? String
                        ?: throw RuntimeException("Auth0 login failed: missing id_token")

        return Auth0TokenResponse(accessToken = accessToken, idToken = idToken)
    }

    fun decodeJwt(token: String): Jwt {
        val parsed = JWTParser.parse(token)
        val claimsSet =
                parsed.jwtClaimsSet
                        ?: throw IllegalArgumentException(
                                "Token is not a JWT (possibly an opaque token)"
                        )
        return Jwt(token, null, null, parsed.header.toJSONObject(), claimsSet.claims)
    }
}
