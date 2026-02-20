package administration.admin.fetchcompanieslist.internal

import administration.admin.domain.commands.fetchcompanieslist.MarkListOfCompaniesFetchedCommand
import administration.admin.fetchcompanieslist.internal.adapter.FetchBoondAPICompanyList
import administration.common.ListOfCompaniesItem
import administration.common.Processor
import administration.events.CompanyListUpdateRequestedEvent
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822854
*/
@Component
class FetchCompaniesListProcessor(
        private val commandGateway: CommandGateway,
        private val adapter: FetchBoondAPICompanyList
) : Processor {

  private val logger = KotlinLogging.logger {}

  // ---------- TRIGGER EVENT ----------
  @EventHandler
  fun on(event: CompanyListUpdateRequestedEvent) {
    logger.info { "CompanyListUpdateRequestedEvent received: $event" }
    fetchAndDispatch(event)
  }

  // ---------- SHARED FLOW ----------
  private fun fetchAndDispatch(event: CompanyListUpdateRequestedEvent) {

    logger.info { "Fetching FetchCompaniesList..." }

    // 1️⃣ Call external system
    val adapterResult = adapter.fetchAll().companies

    // 2️⃣ Map adapter result to domain payload
    val mappedPayload =
            adapterResult.map { item ->
              ListOfCompaniesItem(companyId = item.companyId, companyName = item.companyName)
            }

    logger.info { "Dispatching MarkListOfCompaniesFetchedCommand with ${mappedPayload.size} items" }

    // 3️⃣ Dispatch command
    commandGateway.send<Any>(
                    MarkListOfCompaniesFetchedCommand(
                            settingsId = event.settingsId,
                            connectionId = event.connectionId,
                            listOfCompanies = mappedPayload
                    )
            )
            .exceptionally { throwable ->
              logger.error(throwable) {
                "FAILED to process FetchCompaniesList: ${throwable.message}"
              }
              null
            }
  }
}
