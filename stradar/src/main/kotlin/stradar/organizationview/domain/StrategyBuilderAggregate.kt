package stradar.organizationview.domain

import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import stradar.events.StrategyDraftCreatedEvent
import stradar.organizationview.domain.commands.createdraftstrategy.CreateDraftStrategyCommand

@Aggregate
class StrategyBuilderAggregate() {

        @AggregateIdentifier
        private lateinit var strategyBuilderId: String // teamId-STRATEGY-BUILDER

        private lateinit var teamId: UUID

        private var activeStrategyId: UUID? = null
        private var draftStrategyId: UUID? = null
        private val history: MutableList<UUID> = mutableListOf()

        /**
         * Handles:
         * - First draft → creates aggregate
         * - Subsequent draft → rejected if draft already exists
         */
        @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
        @CommandHandler
        fun handle(cmd: CreateDraftStrategyCommand) {

                // Enforce invariant
                if (draftStrategyId != null) {
                        throw IllegalStateException("There is already a DRAFT strategy.")
                }

                apply(
                        StrategyDraftCreatedEvent(
                                strategyBuilderId = cmd.strategyBuilderId,
                                teamId = cmd.teamId,
                                organizationId = cmd.organizationId,
                                strategyId = cmd.strategyId,
                                strategyName = cmd.strategyName,
                                strategyTimeframe = cmd.strategyTimeframe
                        )
                )
        }

        @EventSourcingHandler
        fun on(event: StrategyDraftCreatedEvent) {
                this.strategyBuilderId = event.strategyBuilderId
                this.teamId = event.teamId
                this.draftStrategyId = event.strategyId
        }
}
