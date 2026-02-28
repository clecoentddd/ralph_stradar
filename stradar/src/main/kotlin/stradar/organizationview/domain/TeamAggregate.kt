package stradar.domain

import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import stradar.common.CommandResult
import stradar.events.TeamCreatedEvent
import stradar.events.TeamDeletedEvent
import stradar.events.TeamUpdatedEvent
import stradar.organizationview.domain.commands.createteam.CreateTeamCommand
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand

@Aggregate
class TeamAggregate() {

        @AggregateIdentifier private lateinit var teamId: UUID
        private lateinit var organizationId: UUID // State needed for validation

        @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
        @CommandHandler
        fun handle(command: CreateTeamCommand): CommandResult {
                // Validation: If already created, don't allow re-creation with different org
                if (this::teamId.isInitialized && this.organizationId != command.organizationId) {
                        throw IllegalStateException(
                                "Team already exists in a different organization"
                        )
                }

                AggregateLifecycle.apply(
                        TeamCreatedEvent(
                                teamId = command.teamId,
                                organizationId = command.organizationId,
                                context = command.context,
                                level = command.level,
                                name = command.name,
                                purpose = command.purpose
                        )
                )

                return CommandResult(command.teamId, AggregateLifecycle.getVersion())
        }

        @CommandHandler
        fun handle(command: DeleteTeamCommand): CommandResult {
                // Requirement: Check teamId and organizationId match
                validateOrganization(command.organizationId)

                AggregateLifecycle.apply(
                        TeamDeletedEvent(
                                teamId = command.teamId,
                                organizationId = command.organizationId
                        )
                )
                return CommandResult(command.teamId, AggregateLifecycle.getVersion())
        }

        @CommandHandler
        fun handle(command: UpdateTeamCommand): CommandResult {
                // 1. Structural Guard: Axon won't call this if the Aggregate isn't found

                // 2. Manual Guard: Ensure this specific instance has an ID loaded
                if (this.teamId != command.teamId) {
                        throw IllegalStateException("Targeting the wrong Team Aggregate instance!")
                }

                // 3. Business Guard: Check Org ID matches (as we discussed)
                validateOrganization(command.organizationId)

                AggregateLifecycle.apply(
                        TeamUpdatedEvent(
                                teamId = this.teamId, // Use the Aggregate's ID, not the command's
                                context = command.context,
                                level = command.level,
                                name = command.name,
                                organizationId = this.organizationId, // Use the Aggregate's Org
                                purpose = command.purpose
                        )
                )

                return CommandResult(this.teamId, AggregateLifecycle.getVersion())
        }

        private fun validateOrganization(providedOrgId: UUID) {
                if (this.organizationId != providedOrgId) {
                        throw IllegalStateException("Organization ID mismatch! Change not allowed.")
                }
        }

        @EventSourcingHandler
        fun on(event: TeamCreatedEvent) {
                this.teamId = event.teamId
                this.organizationId = event.organizationId // Critical: capture the state
        }

        @EventSourcingHandler
        fun on(event: TeamDeletedEvent) {
                // If you want to allow recovery via Update, DO NOT call
                // AggregateLifecycle.markDeleted()
                // Just let the read model handle the "deletion" logic.
        }
}
