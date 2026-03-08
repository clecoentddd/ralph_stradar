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

enum class TeamStatus {
        ACTIVE,
        DELETED
}

@Aggregate
class TeamAggregate() {

        @AggregateIdentifier private lateinit var teamId: UUID
        private lateinit var organizationId: UUID
        private var status: TeamStatus = TeamStatus.ACTIVE

        @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
        @CommandHandler
        fun handle(command: CreateTeamCommand, metaData: MetaData): CommandResult {
                val secureOrgId = metaData.resolveOrganizationId()

                if (this::teamId.isInitialized) {
                        validateOrganization(secureOrgId)
                        throw IllegalStateException("Team ${command.teamId} already exists.")
                }

                AggregateLifecycle.apply(
                        TeamCreatedEvent(
                                teamId = command.teamId,
                                organizationId = secureOrgId,
                                context = command.context,
                                level = command.level,
                                name = command.name,
                                purpose = command.purpose,
                                status = "ACTIVE"
                        ),
                        metaData
                )

                return CommandResult(command.teamId, AggregateLifecycle.getVersion())
        }

        @CommandHandler
        fun handle(command: DeleteTeamCommand, metaData: MetaData): CommandResult {
                validateOrganization(metaData.resolveOrganizationId())

                if (this.status == TeamStatus.DELETED) {
                        throw IllegalStateException("Team is already deleted.")
                }

                AggregateLifecycle.apply(
                        TeamDeletedEvent(
                                teamId = this.teamId,
                                organizationId = this.organizationId,
                                status = "DELETED",
                                reason = command.reason
                        ),
                        metaData
                )
                return CommandResult(this.teamId, AggregateLifecycle.getVersion())
        }

        @CommandHandler
        fun handle(command: UpdateTeamCommand, metaData: MetaData): CommandResult {
                validateOrganization(metaData.resolveOrganizationId())

                AggregateLifecycle.apply(
                        TeamUpdatedEvent(
                                teamId = this.teamId,
                                organizationId = this.organizationId,
                                context = command.context,
                                level = command.level,
                                name = command.name,
                                purpose = command.purpose,
                                status = "ACTIVE"
                        ),
                        metaData
                )

                return CommandResult(this.teamId, AggregateLifecycle.getVersion())
        }

        private fun validateOrganization(trustedOrgId: UUID) {
                if (this.organizationId != trustedOrgId) {
                        throw IllegalStateException("Security Violation: Organization mismatch.")
                }
        }

        @EventSourcingHandler
        fun on(event: TeamCreatedEvent) {
                this.teamId = event.teamId
                this.organizationId = event.organizationId
                this.status = TeamStatus.ACTIVE
        }

        @EventSourcingHandler
        fun on(event: TeamDeletedEvent) {
                this.status = TeamStatus.DELETED
        }

        @EventSourcingHandler
        fun on(event: TeamUpdatedEvent) {
                this.status = TeamStatus.ACTIVE
        }
}
