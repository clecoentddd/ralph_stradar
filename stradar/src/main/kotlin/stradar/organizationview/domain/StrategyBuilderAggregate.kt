package stradar.organizationview.domain

import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import stradar.common.StrategyStatus
import stradar.common.resolveOrganizationId
import stradar.events.StrategyCreatedEvent
import stradar.events.StrategyUpdatedEvent
import stradar.organizationview.domain.commands.createstrategy.CreateStrategyCommand
import stradar.organizationview.domain.commands.updatestrategy.UpdateStrategyCommand

@Aggregate
class StrategyBuilderAggregate() {

        @AggregateIdentifier private lateinit var strategyBuilderId: String

        private lateinit var teamId: UUID

        private var initialized: Boolean = false

        private var activeStrategyId: UUID? = null
        private var draftStrategyId: UUID? = null

        private val history: MutableList<UUID> = mutableListOf()

        /**
         * Create strategy with requested status. Aggregate enforces:
         * - max 1 ACTIVE
         * - max 1 DRAFT
         */
        @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
        @CommandHandler
        fun handle(cmd: CreateStrategyCommand, metaData: MetaData) {

                metaData.resolveOrganizationId()

                when (cmd.strategyStatus) {
                        StrategyStatus.DRAFT -> {
                                if (draftStrategyId != null) {
                                        throw IllegalStateException(
                                                "There is already a DRAFT strategy."
                                        )
                                }
                        }
                        StrategyStatus.ACTIVE -> {
                                if (activeStrategyId != null) {
                                        throw IllegalStateException(
                                                "There is already an ACTIVE strategy."
                                        )
                                }
                        }
                        else -> {
                                // COMPLETED / OBSOLETE / DELETED always allowed
                        }
                }

                apply(
                        StrategyCreatedEvent(
                                strategyBuilderId = cmd.strategyBuilderId,
                                teamId = cmd.teamId,
                                organizationId = cmd.organizationId,
                                strategyId = cmd.strategyId,
                                strategyName = cmd.strategyName,
                                strategyTimeframe = cmd.strategyTimeframe,
                                strategyStatus = cmd.strategyStatus
                        )
                )
        }

        /** Event sourcing updates aggregate state */
        @EventSourcingHandler
        fun on(event: StrategyCreatedEvent) {

                this.strategyBuilderId = event.strategyBuilderId
                this.teamId = event.teamId
                this.initialized = true

                when (event.strategyStatus) {
                        StrategyStatus.DRAFT -> {
                                draftStrategyId = event.strategyId
                        }
                        StrategyStatus.ACTIVE -> {
                                activeStrategyId = event.strategyId
                        }
                        StrategyStatus.COMPLETED,
                        StrategyStatus.OBSOLETE,
                        StrategyStatus.DELETED -> {
                                history.add(event.strategyId)
                        }
                }
        }

        @CommandHandler
        fun handle(cmd: UpdateStrategyCommand, metaData: MetaData) {

                // Guard: reject if this builder has never been initialised
                if (!initialized) {
                        throw IllegalStateException(
                                "Strategy builder '${cmd.strategyBuilderId}' does not exist. " +
                                        "Create a strategy first before updating it."
                        )
                }

                metaData.resolveOrganizationId()

                // Guard: the strategyId must belong to this builder
                val knownIds = listOfNotNull(activeStrategyId, draftStrategyId) + history
                if (cmd.strategyId !in knownIds) {
                        throw IllegalArgumentException(
                                "Strategy '${cmd.strategyId}' is not managed by builder '${cmd.strategyBuilderId}'."
                        )
                }

                // Guard: slot uniqueness — max 1 ACTIVE and 1 DRAFT at any time
                val newStatus = cmd.strategyStatus
                when (newStatus) {
                        StrategyStatus.ACTIVE -> {
                                if (activeStrategyId != null && activeStrategyId != cmd.strategyId
                                ) {
                                        throw IllegalStateException(
                                                "There is already an ACTIVE strategy."
                                        )
                                }
                        }
                        StrategyStatus.DRAFT -> {
                                if (draftStrategyId != null && draftStrategyId != cmd.strategyId) {
                                        throw IllegalStateException(
                                                "There is already a DRAFT strategy. " +
                                                        "Promote or discard it before creating another draft."
                                        )
                                }
                        }
                        else -> {
                                // COMPLETED / OBSOLETE / DELETED always allowed
                        }
                }

                apply(
                        StrategyUpdatedEvent(
                                strategyBuilderId = cmd.strategyBuilderId,
                                organizationId = cmd.organizationId,
                                strategyId = cmd.strategyId,
                                strategyName = cmd.strategyName,
                                strategyStatus = cmd.strategyStatus,
                                strategyTimeframe = cmd.strategyTimeframe,
                                teamId = cmd.teamId
                        )
                )
        }

        @EventSourcingHandler
        fun on(event: StrategyUpdatedEvent) {
                val newStatus = event.strategyStatus
                // Keep slot tracking in sync with the updated status
                when (newStatus) {
                        StrategyStatus.ACTIVE -> {
                                if (draftStrategyId == event.strategyId) draftStrategyId = null
                                activeStrategyId = event.strategyId
                        }
                        StrategyStatus.DRAFT -> {
                                if (activeStrategyId == event.strategyId) activeStrategyId = null
                                draftStrategyId = event.strategyId
                        }
                        StrategyStatus.COMPLETED,
                        StrategyStatus.OBSOLETE,
                        StrategyStatus.DELETED -> {
                                if (activeStrategyId == event.strategyId) activeStrategyId = null
                                if (draftStrategyId == event.strategyId) draftStrategyId = null
                                history.add(event.strategyId)
                        }
                }
        }
}
