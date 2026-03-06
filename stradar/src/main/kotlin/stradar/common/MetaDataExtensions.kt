package stradar.common

import java.util.UUID
import org.axonframework.messaging.MetaData

/**
 * Extension functions for Axon MetaData to consistently resolve security context. Standardizes how
 * organizationId and x-user-id are extracted throughout the organizationview package.
 */
fun MetaData.resolveOrganizationId(): UUID {
        val value =
                this["organizationId"]
                        ?: throw IllegalStateException(
                                "Security context missing: organizationId required"
                        )
        return when (value) {
                is UUID -> value
                is String -> UUID.fromString(value)
                else ->
                        throw IllegalStateException(
                                "Unsupported type for organizationId: ${value::class.java.name}"
                        )
        }
}

fun MetaData.resolveUserId(): String {
        return this["x-user-id"]?.toString()
                ?: throw IllegalStateException("Security context missing: x-user-id required")
}
