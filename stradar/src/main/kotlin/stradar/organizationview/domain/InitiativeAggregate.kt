package stradar.organizationview.domain

import java.util.UUID
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import stradar.events.InitiativeCreatedEvent
import stradar.events.InitiativeItemChangedEvent // You will need to create this event
import stradar.organizationview.domain.commands.changeinitiativeitem.ChangeInitiativeItemCommand
import stradar.organizationview.domain.commands.createinitiative.CreateInitiativeCommand
import stradar.support.metadata.*

@Aggregate
class InitiativeAggregate() {

  @AggregateIdentifier private lateinit var initiativeId: UUID

  // Updated state tracking: Map of ItemID to its content/status
  private val items = mutableMapOf<UUID, String>()

  @CommandHandler
  constructor(command: CreateInitiativeCommand, metaData: MetaData) : this() {
    AggregateLifecycle.apply(
        InitiativeCreatedEvent(
            initiativeId = command.initiativeId,
            initiativeName = command.initiativeName,
            organizationId = command.organizationId,
            strategyId = command.strategyId,
            teamId = command.teamId))
  }

  // --- NEW COMMAND HANDLER ---
  @CommandHandler
  fun handle(command: ChangeInitiativeItemCommand, metaData: MetaData) {
    val userId = metaData[USER_ID_HEADER] as? String

    // Business Logic Example:
    // if (command.content.isBlank()) throw IllegalArgumentException("Content cannot be
    // empty")

    AggregateLifecycle.apply(
        InitiativeItemChangedEvent(
            initiativeId = command.initiativeId,
            step = command.step,
            itemId = command.itemId,
            content = command.content,
            status = command.status))
  }

  @EventSourcingHandler
  fun on(event: InitiativeCreatedEvent) {
    this.initiativeId = event.initiativeId
  }

  // --- NEW EVENT SOURCING HANDLER ---
  @EventSourcingHandler
  fun on(event: InitiativeItemChangedEvent) {
    // We update our internal map so the aggregate "remembers" this item
    if (event.status == "DELETED") {
      items.remove(event.itemId)
    } else {
      items[event.itemId] = event.content
    }
  }
}
