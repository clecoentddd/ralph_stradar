package administration.domain

import administration.admin.domain.commands.fetchcompanieslist.MarkListOfCompaniesFetchedCommand
import administration.admin.domain.commands.fetchinvoicestatemappinglist.MarkListOfInvoiceStateFetchedCommand
import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.admin.domain.commands.requestcompanylistupdate.RequestCompanyListUpdateCommand
import administration.admin.domain.commands.requestinvoicestatemappingupdate.RequestInvoiceStateMappingUpdateCommand
import administration.common.CommandException
import administration.common.CompanyDetails
import administration.common.SettingsConstants
import administration.events.CompanyListUpdateRequestedEvent
import administration.events.InvoiceStateMappingFetchedEvent
import administration.events.InvoiceStateMappingUpdateRequestedEvent
import administration.events.ListOfCompaniesFetchedEvent
import administration.events.SettingsCreatedEvent
import java.util.UUID
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class SettingsAggregate() {

  @AggregateIdentifier private lateinit var settingsId: UUID

  var listOfCompanies: List<CompanyDetails> = emptyList()
  var connectionId: UUID? = null
  var state: String = "INCOMPLETE"

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: CreateSettingsCommand) {
    // Use the companion logger
    if (::settingsId.isInitialized) {
      if (command.settingsId != settingsId) {
        throw CommandException("Cannot create SettingsAggregate with a different settingsId")
      }
      logger.info { "SettingsAggregate already exists for ID: $settingsId" }
      return
    }

    require(command.settingsId == SettingsConstants.SETTINGS_ID) {
      "Invalid settingsId. Expected ${SettingsConstants.SETTINGS_ID}."
    }

    AggregateLifecycle.apply(
            SettingsCreatedEvent(
                    settingsId = SettingsConstants.SETTINGS_ID,
                    connectionId = command.connectionId
            )
    )
  }

  @CommandHandler
  fun handle(command: RequestCompanyListUpdateCommand) {
    AggregateLifecycle.apply(
            CompanyListUpdateRequestedEvent(
                    settingsId = command.settingsId,
                    connectionId = command.connectionId
            )
    )
  }

  @EventSourcingHandler
  fun on(event: SettingsCreatedEvent) {
    this.settingsId = event.settingsId
    this.connectionId = event.connectionId
    this.state = "INITIALIZED"
  }

  @CommandHandler
  fun handle(command: MarkListOfCompaniesFetchedCommand) {
    AggregateLifecycle.apply(
            ListOfCompaniesFetchedEvent(
                    settingsId = command.settingsId,
                    connectionId = command.connectionId,
                    listOfCompanies = command.listOfCompanies
            )
    )
  }

  @EventSourcingHandler
  fun on(event: ListOfCompaniesFetchedEvent) {
    this.listOfCompanies = event.listOfCompanies
  }

  @CommandHandler
  fun handle(command: RequestInvoiceStateMappingUpdateCommand) {
    AggregateLifecycle.apply(
            InvoiceStateMappingUpdateRequestedEvent(
                    settingsId = command.settingsId,
                    connectionId = command.connectionId
            )
    )
  }

  @CommandHandler
  fun handle(command: MarkListOfInvoiceStateFetchedCommand) {
    AggregateLifecycle.apply(
            InvoiceStateMappingFetchedEvent(
                    settingsId = command.settingsId,
                    connectionId = command.connectionId,
                    listOfInvoiceStates = command.listOfInvoiceStates
            )
    )
  }

  // Note: Re-assigning settingsId in every EventSourcingHandler is technically
  // redundant if it's already set in SettingsCreatedEvent, but harmless.

  companion object {
    private val logger = KotlinLogging.logger {}
  }
}
