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
        private lateinit var strategyBuilderId: String // Format: teamId-STRATEGY-BUILDER

        private lateinit var teamId: UUID

        private var activeStrategyId: UUID? = null
        private var draftStrategyId: UUID? = null
        private val history: MutableList<UUID> = mutableListOf()

        /**
         * Handles both:
         * - First draft ever → creates aggregate
         * - Subsequent draft attempts → validates invariant
         */
        @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
        @CommandHandler
        fun handle(cmd: CreateDraftStrategyCommand) {

                // If aggregate already exists, enforce invariant
                if (::strategyBuilderId.isInitialized) {
                        if (draftStrategyId != null) {
                                throw IllegalStateException("There is already a DRAFT strategy.")
                        }
                }

                apply(
                        StrategyDraftCreatedEvent(
                                strategyBuilderId = cmd.strategyBuilderId,
                                teamId = cmd.teamId,
                                organizationId = cmd.organizationId,
                                strategyId = cmd.strategy.strategyId,
                                strategyName = cmd.strategy.title,
                                timeframe = cmd.strategy.timeframe
                        )
                )
        }

        // --- Event Sourcing Handlers ---

        @EventSourcingHandler
        fun on(event: StrategyDraftCreatedEvent) {
                this.strategyBuilderId = event.strategyBuilderId
                this.teamId = event.teamId
                this.draftStrategyId = event.strategyId
        }

        /*
        // Example future handlers

        @EventSourcingHandler
        fun on(event: StrategyActivatedEvent) {
            this.activeStrategyId = event.strategyId
            this.draftStrategyId = null
        }

        @EventSourcingHandler
        fun on(event: StrategyCompletedEvent) {
            this.activeStrategyId?.let { history.add(it) }
            this.activeStrategyId = null
        }

        @EventSourcingHandler
        fun on(event: StrategyDeletedEvent) {
            if (draftStrategyId == event.strategyId) draftStrategyId = null
            if (activeStrategyId == event.strategyId) activeStrategyId = null
            history.remove(event.strategyId)
        }
        */
}
