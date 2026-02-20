package administration.client.fetchinvoices.internal

import administration.client.domain.commands.fetchinvoices.MarkInvoicesFetchedCommand
import administration.client.fetchinvoices.internal.adapter.FetchBoondAPIInvoiceList
import administration.common.Processor
import administration.events.OrdersFetchedEvent
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660086962408
*/
@Component
class FetchInvoiceListProcessor(
    private val commandGateway: CommandGateway,
    private val adapter: FetchBoondAPIInvoiceList
) : Processor {

  private val logger = KotlinLogging.logger {}

  // ---------- TRIGGER EVENT ----------
  @EventHandler
  fun on(event: OrdersFetchedEvent) {
    logger.info { "OrdersFetchedEvent received: $event" }
    fetchAndDispatch(event)
  }

  // ---------- SHARED FLOW ----------
  private fun fetchAndDispatch(event: OrdersFetchedEvent) {

    logger.info { "Fetching FetchInvoiceList..." }

    // 1️⃣ Call external system
    val adapterResult = adapter.fetch(event.companyId)

    logger.info {
      "Dispatching MarkInvoicesFetchedCommand with ${adapterResult.invoices.size} items"
    }

    // 3️⃣ Dispatch command
    commandGateway
        .send<Any>(
            MarkInvoicesFetchedCommand(
                companyId = event.companyId,
                clientId = event.clientId,
                invoiceList = adapterResult.invoices))
        .exceptionally { throwable ->
          logger.error(throwable) { "FAILED to process FetchInvoiceList: ${throwable.message}" }
          null
        }
  }
}
