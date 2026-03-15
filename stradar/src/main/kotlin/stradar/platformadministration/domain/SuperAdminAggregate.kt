package stradar.domain

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import stradar.common.CommandResult
import stradar.events.SuperAdminSignedInEvent
import stradar.platformadministration.domain.commands.signinadmin.SignInAdminCommand

@Aggregate
class SuperAdminAggregate() {

  private val logger = KotlinLogging.logger {}

  @AggregateIdentifier private var adminAccountId: UUID? = null
  private var username: String? = null

  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  @CommandHandler
  fun handle(command: SignInAdminCommand): CommandResult {

    // 🛡️ Guard: If this is the first time (Genesis), only 'superadmin' is allowed
    if (this.adminAccountId == null) {
      require(command.username == "superadmin") {
        "Unauthorized: Only the 'superadmin' username can initialize the platform slot."
      }
      logger.info { "🌱 Initializing Genesis Super Admin slot for: ${command.username}" }
    } else {
      // Guard: Prevent other usernames from trying to "take over" the fixed ID slot
      require(command.username == this.username) {
        "Unauthorized: Username mismatch for this administrative slot."
      }
    }

    AggregateLifecycle.apply(
        SuperAdminSignedInEvent(
            adminAccountId = command.adminAccountId,
            username = command.username.lowercase().trim()))

    return CommandResult(
        identifier = command.adminAccountId, aggregateSequence = AggregateLifecycle.getVersion())
  }

  @EventSourcingHandler
  fun on(event: SuperAdminSignedInEvent) {
    this.adminAccountId = event.adminAccountId
    this.username = event.username
  }
}
