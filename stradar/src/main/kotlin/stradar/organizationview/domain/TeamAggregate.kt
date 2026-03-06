package stradar.domain

import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import stradar.common.CommandResult
import stradar.common.resolveOrganizationId
import stradar.events.TeamCreatedEvent
import stradar.events.TeamDeletedEvent
import stradar.events.TeamUpdatedEvent
import stradar.organizationview.domain.commands.createteam.CreateTeamCommand
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand

@Aggregate
class TeamAggregate() {

        @AggregateIdentifier private lateinit var teamId: UUID
        private lateinit var organizationId: UUID

        @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
        @CommandHandler
        fun handle(command: CreateTeamCommand, metaData: MetaData): CommandResult {
                val secureOrgId = metaData.resolveOrganizationId()

                // Guard: Prevent overwriting an existing aggregate belonging to another Org
                if (this::teamId.isInitialized) {
                        validateOrganization(secureOrgId)
                        throw IllegalStateException("Team ${command.teamId} already exists.")
                }

                AggregateLifecycle.apply(
                        TeamCreatedEvent(
                                teamId = command.teamId,
                                organizationId = secureOrgId, // TRUST the metadata for creation
                                context = command.context,
                                level = command.level,
                                name = command.name,
                                purpose = command.purpose
                        )
                )

                return CommandResult(command.teamId, AggregateLifecycle.getVersion())
        }

        @CommandHandler
        fun handle(command: DeleteTeamCommand, metaData: MetaData): CommandResult {
                // Guard: Ensure the requester owns this resource
                validateOrganization(metaData.resolveOrganizationId())

                AggregateLifecycle.apply(
                        TeamDeletedEvent(teamId = this.teamId, organizationId = this.organizationId)
                )
                return CommandResult(this.teamId, AggregateLifecycle.getVersion())
        }

        @CommandHandler
        fun handle(command: UpdateTeamCommand, metaData: MetaData): CommandResult {
                // Guard: Ensure the requester owns this resource
                validateOrganization(metaData.resolveOrganizationId())

                AggregateLifecycle.apply(
                        TeamUpdatedEvent(
                                teamId = this.teamId,
                                organizationId = this.organizationId,
                                context = command.context,
                                level = command.level,
                                name = command.name,
                                purpose = command.purpose
                        )
                )

                return CommandResult(this.teamId, AggregateLifecycle.getVersion())
        }

        /**
         * Encapsulated Security Check Compares the trusted ID from the MetaData against the
         * Aggregate's owner
         */
        private fun validateOrganization(trustedOrgId: UUID) {
                if (this.organizationId != trustedOrgId) {
                        throw IllegalStateException("Security Violation: Organization mismatch.")
                }
        }

        @EventSourcingHandler
        fun on(event: TeamCreatedEvent) {
                this.teamId = event.teamId
                this.organizationId = event.organizationId
        }

        @EventSourcingHandler
        fun on(event: TeamDeletedEvent) {
                // Optional: AggregateLifecycle.markDeleted() if you want to block further commands
        }
}
