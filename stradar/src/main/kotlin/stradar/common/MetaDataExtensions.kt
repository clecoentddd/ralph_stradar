package stradar.common

import java.util.UUID
import org.axonframework.messaging.MetaData
import stradar.support.metadata.*

/**
 * Extension functions for Axon MetaData to consistently resolve security context. Standardizes how
 * organizationId and x-user-id are extracted throughout the organizationview package.
 */
fun MetaData.resolveOrganizationId(): UUID {
        val value =
                this[ORGANIZATION_ID_HEADER]
                        ?: throw IllegalStateException(
                                "Security context missing: $ORGANIZATION_ID_HEADER required"
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
        return this[USER_ID_HEADER]?.toString()
                ?: throw IllegalStateException("Security context missing: $USER_ID_HEADER required")
}
