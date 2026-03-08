package stradar.domain

import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import stradar.common.CommandException
import stradar.events.OrganizationDefinedEvent
import stradar.platformadministration.domain.commands.defineorganization.DefineOrganizationCommand

@Aggregate
class OrganizationAggregate {

  @AggregateIdentifier private var organizationId: UUID? = null
  private var personId: UUID? = null

  // Required for Axon
  constructor()

  // Change 'fun handle' to 'constructor'
  @CommandHandler
  constructor(command: DefineOrganizationCommand) : this() {

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
  }

  @EventSourcingHandler
  fun on(event: OrganizationDefinedEvent) {
    this.organizationId = event.organizationId
    this.personId = event.personId
  }
}
