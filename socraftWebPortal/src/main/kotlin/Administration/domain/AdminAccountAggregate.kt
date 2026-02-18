package administration.domain

import administration.domain.commands.adminconnection.ToConnectCommand
import administration.events.AdminConnectedEvent
import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class AdminAccountAggregate {

  @AggregateIdentifier var connectionId: UUID? = null
  var email: String? = null

  // Default constructor for Axon
  constructor()

  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  @CommandHandler
  fun handle(command: ToConnectCommand) {

    // 1. Validation: connectionId must not be null (redundant if using @TargetAggregateIdentifier,
    // but good for spec safety)
    require(command.connectionId != null) { "ConnectionId must not be null" }

    // 2. Validation: email must be present and not blank
    require(!command.email.isNullOrBlank()) { "Email must not be null or empty" }

    // 3. Optional: Add regex check for email format if needed
    // require(command.email.contains("@")) { "Invalid email format" }

    AggregateLifecycle.apply(
            AdminConnectedEvent(connectionId = command.connectionId, email = command.email)
    )
  }

  @EventSourcingHandler
  fun on(event: AdminConnectedEvent) {
    // Hydrate the aggregate state
    this.connectionId = event.connectionId
    this.email = event.email
  }
}
