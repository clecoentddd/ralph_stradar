package administration.domain

import administration.client.domain.commands.clientaccountconnection.ToConnectToAccountCommand
import administration.client.domain.commands.createclientaccount.CreateAccountCommand
import administration.common.CommandException
import administration.events.AccountCreatedEvent
import administration.events.ClientConnectedEvent
import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class ClientAccountAggregate {

    @AggregateIdentifier var clientId: UUID? = null

    var clientEmail: String? = null
    var companyId: Long? = null
    var connectionId: UUID? = null

    constructor() // Required by Axon

    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    @CommandHandler
    fun handle(command: CreateAccountCommand): Long {
        // FIX: If clientEmail is already set, this aggregate instance exists.
        // We must throw the exception that the test is looking for.
        if (this.clientEmail != null) {
            throw CommandException("Account already exists with email: ${command.clientEmail}")
        }

        AggregateLifecycle.apply(
                AccountCreatedEvent(
                        clientEmail = command.clientEmail,
                        companyId = command.companyId,
                        connectionId = command.connectionId,
                        clientId = command.clientId
                )
        )
        return command.companyId
    }

    @EventSourcingHandler
    fun on(event: AccountCreatedEvent) {
        clientId = event.clientId
        clientEmail = event.clientEmail
        companyId = event.companyId
        connectionId = event.connectionId
    }

    // Connection to their account
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    @CommandHandler
    fun handle(command: ToConnectToAccountCommand): Long {

        // 1. Validation Logic
        val currentCompanyId =
                checkNotNull(this.companyId) {
                    "Identity Error: Account ${command.clientId} exists but has no associated CompanyId. Connection refused."
                }

        if (this.clientEmail != command.clientEmail) {
            throw IllegalArgumentException(
                    "Security Error: Email mismatch for account ${command.clientId}"
            )
        }

        // 2. Apply the Event
        AggregateLifecycle.apply(
                ClientConnectedEvent(
                        clientId = command.clientId,
                        clientEmail = command.clientEmail,
                        companyId = currentCompanyId
                )
        )
        return currentCompanyId
    }

    @EventSourcingHandler
    fun on(event: ClientConnectedEvent) {
        clientId = event.clientId
        clientEmail = event.clientEmail
        companyId = event.companyId
    }
}
