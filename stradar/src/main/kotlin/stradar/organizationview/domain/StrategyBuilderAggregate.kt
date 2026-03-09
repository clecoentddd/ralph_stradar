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
import stradar.organizationview.domain.commands.createstrategy.CreateStrategyCommand

@Aggregate
class StrategyBuilderAggregate() {

        @AggregateIdentifier private lateinit var strategyBuilderId: String

        private lateinit var teamId: UUID

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
}
