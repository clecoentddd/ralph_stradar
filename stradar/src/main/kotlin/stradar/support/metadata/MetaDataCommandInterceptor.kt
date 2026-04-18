/* (C)2025 */
package stradar.support.metadata

import java.util.function.BiFunction
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageDispatchInterceptor
import org.springframework.stereotype.Component
import stradar.platformadministration.domain.commands.signinadmin.SignInAdminCommand

@Component
class MetaDataCommandInterceptor : MessageDispatchInterceptor<CommandMessage<Any>> {

  override fun handle(
          messages: List<CommandMessage<Any>>
  ): BiFunction<Int, CommandMessage<Any>, CommandMessage<Any>> {

    return BiFunction { _, message ->
      val payload = message.payload

      // ✅ QUICK FIX: bypass session check for login command
      if (payload is SignInAdminCommand) {
        return@BiFunction message
      }

      // 🔒 enforce session for all other commands
      if (!message.metaData.containsKey(SESSION_ID_HEADER)) {
        throw IllegalArgumentException("Missing required header: X-Session-Id")
      }

      message
    }
  }
}

y
