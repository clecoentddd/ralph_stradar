package stradar.domain

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import stradar.common.*
import stradar.events.*
import stradar.organizationview.domain.commands.deleteenvironmentalchange.DeleteEnvironmentalChangeCommand
import stradar.organizationview.domain.commands.detectenvironmentalchange.DetectEnvironmentalChangeCommand
import stradar.organizationview.domain.commands.updateenvironmentalchange.UpdateEnvironmentalChangeCommand

@Aggregate
class EnvironmentalChangeAggregate() {

    private val logger = KotlinLogging.logger {}

    @AggregateIdentifier private lateinit var environmentalChangeId: UUID
    private lateinit var teamId: UUID
    private lateinit var organizationId: UUID

    // Internal state to track if it's already deleted
    private var isDeleted: Boolean = false

    /* ===================================================== */
    /* ================ Detect (Creation) =================== */
    /* ===================================================== */

    @CommandHandler
    constructor(command: DetectEnvironmentalChangeCommand) : this() {
        logger.info { "🌱 Detecting Environmental Change: ${command.environmentalChangeId}" }

        AggregateLifecycle.apply(
                EnvironmentalChangeDetectedEvent(
                        environmentalChangeId = command.environmentalChangeId,
                        teamId = command.teamId,
                        organizationId = command.organizationId,
                        title = command.title,
                        detect = command.detect,
                        assess = command.assess,
                        respond = command.respond,
                        type = command.type,
                        category = command.category,
                        distance = command.distance,
                        impact = command.impact,
                        risk = command.risk
                )
        )
    }

    /* ===================================================== */
    /* ================ Update Change ====================== */
    /* ===================================================== */

    @CommandHandler
    fun handle(command: UpdateEnvironmentalChangeCommand) {
        if (isDeleted) throw IllegalStateException("Cannot update a deleted environmental change.")

        logger.info { "🔄 Updating Environmental Change: $environmentalChangeId" }

        AggregateLifecycle.apply(
                EnvironmentalChangeUpdatedEvent(
                        environmentalChangeId = this.environmentalChangeId,
                        teamId = this.teamId,
                        organizationId = this.organizationId,
                        title = command.title,
                        detect = command.detect,
                        assess = command.assess,
                        respond = command.respond,
                        type = command.type,
                        category = command.category,
                        distance = command.distance,
                        impact = command.impact,
                        risk = command.risk
                )
        )
    }

    /* ===================================================== */
    /* ================ Delete Change ====================== */
    /* ===================================================== */

    @CommandHandler
    fun handle(command: DeleteEnvironmentalChangeCommand): CommandResult {
        if (isDeleted) throw IllegalStateException("Change already deleted.")

        logger.info { "🗑 Deleting Environmental Change: $environmentalChangeId" }

        AggregateLifecycle.apply(
                EnvironmentalChangeDeletedEvent(
                        environmentalChangeId = this.environmentalChangeId,
                        teamId = this.teamId,
                        organizationId = this.organizationId
                )
        )

        return CommandResult(this.teamId, AggregateLifecycle.getVersion())
    }

    /* ===================================================== */
    /* ================ Event Sourcing Handlers ============ */
    /* ===================================================== */

    @EventSourcingHandler
    fun on(event: EnvironmentalChangeDetectedEvent) {
        this.environmentalChangeId = event.environmentalChangeId
        this.teamId = event.teamId
        this.organizationId = event.organizationId
        this.isDeleted = false
    }

    @EventSourcingHandler
    fun on(event: EnvironmentalChangeUpdatedEvent) {
        // No state update needed unless we start tracking specific fields in the aggregate
    }

    @EventSourcingHandler
    fun on(event: EnvironmentalChangeDeletedEvent) {
        this.isDeleted = true
        // Technically Axon can mark the aggregate as deleted:
        // AggregateLifecycle.markDeleted()
    }
}
