package administration.domain

import administration.client.domain.commands.clientaccountconnection.ToConnectToAccountCommand
import administration.client.domain.commands.createclientaccount.CreateAccountCommand
import administration.events.AccountCreatedEvent
import administration.events.CustomerConnectedEvent
import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class AccountAggregate {

    @AggregateIdentifier var clientId: UUID? = null

    var clientEmail: String? = null
    var companyId: Long? = null
    var connectionId: UUID? = null

    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    @CommandHandler
    fun handle(command: CreateAccountCommand) {
        if (clientEmail != null && clientEmail != command.clientEmail) {
            throw IllegalArgumentException("Email does not match existing account email")
        }

        AggregateLifecycle.apply(
                AccountCreatedEvent(
                        clientEmail = command.clientEmail,
                        companyId = command.companyId,
                        connectionId = command.connectionId,
                        clientId = command.clientId
                )
        )
    }

    @EventSourcingHandler
    fun on(event: AccountCreatedEvent) {
        // handle event
        clientId = event.clientId
        clientEmail = event.clientEmail
        companyId = event.companyId
        connectionId = event.connectionId
    }

    // Connection to their account
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    @CommandHandler
    fun handle(command: ToConnectToAccountCommand) {

        // 1. Validation Logic
        // We use checkNotNull to satisfy the compiler and enforce business rules
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
        // currentCompanyId is now a non-nullable Long
        AggregateLifecycle.apply(
                CustomerConnectedEvent(
                        clientId = command.clientId,
                        clientEmail = command.clientEmail,
                        companyId = currentCompanyId
                )
        )
    }

    @EventSourcingHandler
    fun on(event: CustomerConnectedEvent) {
        // handle event
        clientId = event.clientId
        clientEmail = event.clientEmail
        companyId = event.companyId
    }
}
