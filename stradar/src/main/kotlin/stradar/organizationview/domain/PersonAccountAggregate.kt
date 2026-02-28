package stradar.domain

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import stradar.events.OrganizationDefinedEvent
import stradar.events.PersonCreatedEvent
import stradar.events.PersonSignedInEvent
import stradar.organizationview.domain.commands.createperson.CreatePersonCommand
import stradar.organizationview.domain.commands.signin.SignInCommand

@Aggregate
class PersonAccountAggregate {

        private val logger = KotlinLogging.logger {}

        @AggregateIdentifier private var personId: UUID? = null
        private var organizationId: UUID? = null
        private var organizationName: String? = null // 🛡️ Added missing field
        private var role: String? = null
        private var username: String? = null

        constructor()

        @CommandHandler
        constructor(command: CreatePersonCommand, metadata: MetaData) : this() {
                val creatorPersonId = metadata["x-user-id"]?.toString()

                logger.info { "Creating Person: ${command.personId} (Creator: $creatorPersonId)" }

                AggregateLifecycle.apply(
                        PersonCreatedEvent(
                                personId = command.personId,
                                organizationId = command.organizationId,
                                username = command.username,
                                organizationName = command.organizationName,
                                role = command.role
                        )
                )
        }

        @CommandHandler
        fun handle(command: SignInCommand, metadata: MetaData) {
                val sessionPersonId = metadata["x-user-id"]?.toString()

                // 1. Validation
                if (sessionPersonId != null && sessionPersonId != command.personId.toString()) {
                        throw IllegalAccessException("Security Error: Identity mismatch.")
                }

                // 2. Fetch current state for the event
                val currentOrgId =
                        this.organizationId
                                ?: throw IllegalStateException("No Organization assigned.")
                val currentRole = this.role ?: throw IllegalStateException("No Role assigned.")

                // 3. Emit sign-in (Using existing state)
                AggregateLifecycle.apply(
                        PersonSignedInEvent(
                                personId = command.personId,
                                organizationId = currentOrgId,
                                role = currentRole
                        )
                )
        }

        @EventSourcingHandler
        fun on(event: PersonCreatedEvent) {
                this.personId = event.personId
                this.organizationId = event.organizationId
                this.organizationName = event.organizationName
                this.role = event.role
                this.username = event.username
        }

        @EventSourcingHandler
        fun on(event: PersonSignedInEvent) {
                // No state update needed here usually, but we keep the personId sync for safety
                this.personId = event.personId
        }

        @EventSourcingHandler
        fun on(event: OrganizationDefinedEvent) {
                this.personId = event.personId
                this.organizationId = event.organizationId
                this.role = "ADMIN" // Assuming organization definer is an Admin
                this.username = event.username
        }
}
