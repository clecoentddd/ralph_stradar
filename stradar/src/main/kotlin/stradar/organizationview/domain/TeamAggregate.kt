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
import stradar.organizationview.domain.commands.createteam.CreateTeamCommand

@Aggregate
class TeamAggregate {

        @AggregateIdentifier var teamId: UUID? = null

        @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
        @CommandHandler
        fun handle(command: CreateTeamCommand): CommandResult {

                AggregateLifecycle.apply(
                        TeamCreatedEvent(
                                teamId = command.teamId,
                                organizationId = command.organizationId,
                                adminAccountId = command.adminAccountId,
                                organizationName = command.organizationName,
                                context = command.context,
                                level = command.level,
                                name = command.name,
                                purpose = command.purpose
                        )
                )

                return CommandResult(
                        identifier = command.teamId,
                        aggregateSequence = AggregateLifecycle.getVersion()
                )
        }

        @EventSourcingHandler
        fun on(event: TeamCreatedEvent) {
                this.teamId = event.teamId
        }
}
