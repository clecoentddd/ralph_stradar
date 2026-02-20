package administration.domain

import administration.client.domain.commands.fetchinvoices.MarkInvoicesFetchedCommand
import administration.client.domain.commands.fetchorders.MarkOrdersFetchedCommand
import administration.client.domain.commands.fetchprojects.MarkListOfProjectsFetchedCommand
import administration.events.InvoicesFetchedEvent
import administration.events.ListOfProjectsFetchedEvent
import administration.events.OrdersFetchedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class CompanyAggregate {

  @AggregateIdentifier var companyId: Long? = null

  // Use CREATE_IF_MISSING so the first time a project list is fetched,
  // the Company entity is born in your system.
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  @CommandHandler
  fun handle(command: MarkListOfProjectsFetchedCommand) {

    // 1. Validate the External ID
    // We reject 0 or null as they aren't valid unique identifiers for an external system.
    require(command.companyId != null && command.companyId != 0L) {
      "Invalid companyId: ${command.companyId}. Aggregate cannot be created or updated without a valid external ID."
    }

    // 2. Apply the event
    AggregateLifecycle.apply(
        ListOfProjectsFetchedEvent(
            clientId = command.clientId,
            companyId = command.companyId,
            projectList = command.projectList))
  }

  @EventSourcingHandler
  fun on(event: ListOfProjectsFetchedEvent) {

    this.companyId = event.companyId
  }

  // Order List Fetched
  @CommandHandler
  fun handle(command: MarkOrdersFetchedCommand) {

    AggregateLifecycle.apply(
        OrdersFetchedEvent(
            companyId = command.companyId,
            clientId = command.clientId,
            orderList = command.orderList))
  }

  @EventSourcingHandler
  fun on(event: OrdersFetchedEvent) {
    // handle event
    companyId = event.companyId
  }

  // Invoice List
  @CommandHandler
  fun handle(command: MarkInvoicesFetchedCommand) {

    AggregateLifecycle.apply(
        InvoicesFetchedEvent(
            companyId = command.companyId,
            clientId = command.clientId,
            invoiceList = command.invoiceList))
  }

  @EventSourcingHandler
  fun on(event: InvoicesFetchedEvent) {
    // handle event
    companyId = event.companyId
  }

  // Default constructor required by Axon
  constructor()
}
