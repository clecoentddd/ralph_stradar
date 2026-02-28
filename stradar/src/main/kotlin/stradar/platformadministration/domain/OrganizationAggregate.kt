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
class OrganizationAggregate {

  @AggregateIdentifier private var organizationId: UUID? = null
  private var personId: UUID? = null // Using standardized naming

  // Required by Axon for rebuilding the aggregate from events
  constructor()

  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  @CommandHandler
  fun handle(command: DefineOrganizationCommand): CommandResult {

    if (command.organizationName.isBlank()) {
      throw CommandException("Error: Organization name is mandatory")
    }

    // Apply the event using standardized 'personId' and 'role'
    AggregateLifecycle.apply(
            OrganizationDefinedEvent(
                    organizationId = command.organizationId,
                    personId = command.personId,
                    username = command.username,
                    organizationName = command.organizationName,
                    role = "ORGANIZATION_ADMIN"
            )
    )

    return CommandResult(
            identifier = command.organizationId,
            aggregateSequence = AggregateLifecycle.getVersion()
    )
  }

  @EventSourcingHandler
  fun on(event: OrganizationDefinedEvent) {
    // Corrected: Assigning the event data to the standardized class fields
    this.organizationId = event.organizationId
    this.personId = event.personId
  }
}
