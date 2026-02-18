package administration.client.fetchorders.internal

import administration.client.domain.commands.fetchorders.MarkOrdersFetchedCommand
import administration.client.fetchorders.internal.adapter.FetchBoondAPIOrderList
import administration.common.Processor
import administration.events.ListOfProjectsFetchedEvent
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660085256079
*/
@Component
class FetchBoondOrderListProcessor(
        private val commandGateway: CommandGateway,
        private val adapter: FetchBoondAPIOrderList
) : Processor {

    private val logger = KotlinLogging.logger {}

    // ---------- TRIGGER EVENT ----------
    @EventHandler
    fun on(event: ListOfProjectsFetchedEvent) {
        logger.info { "ListOfProjectsFetchedEvent received: $event" }
        fetchAndDispatch(event)
    }

    // ---------- SHARED FLOW ----------
    private fun fetchAndDispatch(event: ListOfProjectsFetchedEvent) {

        logger.info { "Fetching FetchBoondOrderList..." }

        // 1️⃣ Call external system
        val adapterResult = adapter.fetch(event.companyId)

        logger.info {
            "Dispatching MarkOrdersFetchedCommand with ${adapterResult.orders.size} items"
        }

        // 3️⃣ Dispatch command
        commandGateway.send<Any>(
                        MarkOrdersFetchedCommand(
                                companyId = event.companyId,
                                clientId = event.clientId,
                                orderList = adapterResult.orders
                        )
                )
                .exceptionally { throwable ->
                    logger.error(throwable) {
                        "FAILED to process FetchBoondOrderList: ${throwable.message}"
                    }
                    null
                }
    }
}
