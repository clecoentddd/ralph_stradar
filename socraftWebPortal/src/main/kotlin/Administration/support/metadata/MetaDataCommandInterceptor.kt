package administration.support.metadata

import java.util.function.BiFunction
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageDispatchInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class MetaDataCommandInterceptor : MessageDispatchInterceptor<CommandMessage<*>> {
    // MetaDataCommandInterceptor.kt

    override fun handle(
            messages: List<CommandMessage<*>>
    ): BiFunction<Int, CommandMessage<*>, CommandMessage<*>> {
        return BiFunction { _, message ->
            val payloadTypeName = message.payloadType.name

            if (payloadTypeName.contains("administration.admin")) {

                // 1. Try to get the session from the Web Request (for Controllers)
                val attributes =
                        RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
                val webSessionId = attributes?.request?.getHeader(AdminSecurityHeaders.SESSION_ID)

                // 2. Try to get the session from the Message itself (for background Processors)
                val existingSessionId = message.metaData[AdminSecurityHeaders.SESSION_ID] as? String

                val finalSessionId = webSessionId ?: existingSessionId

                // 3. Only throw if BOTH are missing
                if (finalSessionId.isNullOrBlank()) {
                    throw IllegalArgumentException(
                            "Missing required header: ${AdminSecurityHeaders.SESSION_ID}"
                    )
                }

                // 4. Return the message with the metadata (re-injecting it if it came from the web)
                return@BiFunction message.andMetaData(
                        mapOf(AdminSecurityHeaders.SESSION_ID to finalSessionId)
                )
            }

            message
        }
    }
}
