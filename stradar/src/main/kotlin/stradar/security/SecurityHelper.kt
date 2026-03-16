package stradar.security

import java.util.UUID
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

const val COMPANY_ID_CLAIM = "https://stradar.com/companyId"

/** Extracted JWT claims from the authenticated user. */
data class AuthenticatedUser(
        val auth0UserId: String, // e.g. "auth0|69b51dcd..."
        val companyId: UUID?, // the organizationId from JWT
        val email: String?,
        val username: String?
)

@Component
class SecurityHelper {

    /**
     * Extracts claims from the JWT token. Use this in every controller to get the authenticated
     * user's identity.
     *
     * Usage: val user = securityHelper.extractUser(authentication)
     */
    fun extractUser(authentication: Authentication): AuthenticatedUser {
        val jwt = authentication.principal as Jwt
        val companyIdStr = jwt.getClaimAsString(COMPANY_ID_CLAIM)
        return AuthenticatedUser(
                auth0UserId = jwt.subject,
                companyId = companyIdStr?.let { runCatching { UUID.fromString(it) }.getOrNull() },
                email = jwt.getClaimAsString("email"),
                username = jwt.getClaimAsString("nickname")
        )
    }

    /**
     * Checks that the authenticated user belongs to the given organizationId. Returns 403
     * ResponseEntity if not, null if OK.
     *
     * Usage: securityHelper.checkOrganization(user, organizationId)?.let { return it }
     */
    fun <T> checkOrganization(user: AuthenticatedUser, organizationId: UUID?): ResponseEntity<T>? {
        if (user.companyId == null || organizationId == null) {
            logger.warn {
                "Missing companyId in token or request. token=${user.companyId} request=$organizationId"
            }
            return ResponseEntity.status(403).build()
        }
        if (user.companyId != organizationId) {
            logger.warn {
                "OrganizationId mismatch: token=${user.companyId} entity=$organizationId"
            }
            return ResponseEntity.status(403).build()
        }
        return null
    }

    /**
     * Checks that the authenticated user is the same person as the requested personId. Compares via
     * auth0UserId stored on the entity. Returns 403 ResponseEntity if not, null if OK.
     *
     * Usage: securityHelper.checkSameUser(user, entity.auth0UserId)?.let { return it }
     */
    fun <T> checkSameUser(user: AuthenticatedUser, entityAuth0UserId: String?): ResponseEntity<T>? {
        if (entityAuth0UserId == null || user.auth0UserId != entityAuth0UserId) {
            logger.warn {
                "Auth0 userId mismatch: token=${user.auth0UserId} entity=$entityAuth0UserId"
            }
            return ResponseEntity.status(403).build()
        }
        return null
    }
}
