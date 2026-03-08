package stradar.domain

import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import stradar.common.CommandException
import stradar.common.CommandResult
import stradar.events.OrganizationDefinedEvent
import stradar.platformadministration.domain.commands.defineorganization.DefineOrganizationCommand

@Aggregate
class OrganizationAggregate() {

  @AggregateIdentifier private var organizationId: UUID? = null
  private var personId: UUID? = null

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: DefineOrganizationCommand): CommandResult {
    // Validation logic - relies on command values
    if (command.organizationName.isBlank()) {
      throw CommandException("organizationName is required and cannot be empty")
    }

    AggregateLifecycle.apply(
            OrganizationDefinedEvent(
                    organizationId = command.organizationId,
                    personId = command.personId,
                    username = command.username,
                    organizationName = command.organizationName,
                    role = "ORGANIZATION_ADMIN"
            )
    )

    // Now we return the object the Controller expects
    return CommandResult(command.organizationId, AggregateLifecycle.getVersion())
  }

  @EventSourcingHandler
  fun on(event: OrganizationDefinedEvent) {
    this.organizationId = event.organizationId
    this.personId = event.personId
  }
}
