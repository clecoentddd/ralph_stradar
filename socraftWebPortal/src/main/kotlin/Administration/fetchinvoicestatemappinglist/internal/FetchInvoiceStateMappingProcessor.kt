package administration.fetchinvoicestatemappinglist.internal

import administration.common.ListOfInvoiceStatesItem
import administration.common.Processor
import administration.domain.commands.fetchinvoicestatemappinglist.MarkListOfInvoiceStateFetchedCommand
import administration.events.InvoiceStateMappingUpdateRequestedEvent
import administration.fetchinvoicestatemappinglist.internal.adapter.FetchBoondAPIInvoiceStateMapping
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713536
*/
@Component
class FetchInvoiceStateMappingProcessor(
        private val commandGateway: CommandGateway,
        private val adapter: FetchBoondAPIInvoiceStateMapping
) : Processor {

    private val logger = KotlinLogging.logger {}

    // ---------- TRIGGER EVENT ----------
    @EventHandler
    fun on(event: InvoiceStateMappingUpdateRequestedEvent) {
        logger.info { "InvoiceStateMappingUpdateRequestedEvent received: $event" }
        fetchAndDispatch(event)
    }

    // ---------- SHARED FLOW ----------
    private fun fetchAndDispatch(event: InvoiceStateMappingUpdateRequestedEvent) {

        logger.info { "Fetching FetchInvoiceStateMapping..." }

        // 1️⃣ Call external system
        val adapterResult = adapter.fetchAll()

        // 2️⃣ Map adapter result to domain payload
        val mappedPayload =
                adapterResult.states.map { item ->
                    ListOfInvoiceStatesItem(code = item.code, label = item.label)
                }

        logger.info {
            "Dispatching MarkListOfInvoiceStateFetchedCommand with ${mappedPayload.size} items"
        }

        // 3️⃣ Dispatch command
        commandGateway.send<Any>(
                        MarkListOfInvoiceStateFetchedCommand(
                                settingsId = event.settingsId,
                                connectionId = event.connectionId,
                                listOfInvoiceStates = mappedPayload
                        )
                )
                .exceptionally { throwable ->
                    logger.error(throwable) {
                        "FAILED to process FetchInvoiceStateMapping: ${throwable.message}"
                    }
                    null
                }
    }
}
