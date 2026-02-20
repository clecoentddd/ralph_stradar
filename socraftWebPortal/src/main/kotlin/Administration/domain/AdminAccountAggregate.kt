package administration.domain

import administration.admin.domain.commands.adminconnection.ToConnectCommand
import administration.common.CommandException
import administration.events.AdminConnectedEvent
import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class AdminAccountAggregate {

  @AggregateIdentifier var connectionId: UUID? = null
  var email: String? = null

  // 1. Required empty constructor for Axon
  constructor()

  // 2. Command Handling Constructor (Replaces the 'handle' function)
  @CommandHandler
  constructor(command: ToConnectCommand) : this() {
    // Validation: connectionId must not be null
    require(command.connectionId != null) { "ConnectionId must not be null" }

    // Validation: email must be present and not blank
    require(!command.email.isNullOrBlank()) { "Email must not be null or empty" }

    // Validation: email must be a socraft.ch email
    if (!command.email.trim().lowercase().endsWith("@socraft.ch")) {
      throw CommandException("Email must be a socraft.ch email")
    }

    // Apply the event
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
