package administration.client.fetchprojects.internal

import administration.client.domain.commands.fetchprojects.MarkListOfProjectsFetchedCommand
import administration.client.fetchprojects.internal.adapter.FetchBoondAPIProjectList
import administration.common.ListOfProjectsItem
import administration.common.Processor
import administration.events.CustomerConnectedEvent
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660044053421
*/
@Component
class FetchBoondProjectListProcessor(
        private val commandGateway: CommandGateway,
        private val adapter: FetchBoondAPIProjectList
) : Processor {

    private val logger = KotlinLogging.logger {}

    // ---------- TRIGGER EVENT ----------
    @EventHandler
    fun on(event: CustomerConnectedEvent) {
        logger.info { "CustomerConnectedEvent received: $event" }
        fetchAndDispatch(event)
    }

    // ---------- SHARED FLOW ----------
    private fun fetchAndDispatch(event: CustomerConnectedEvent) {

        logger.info { "Fetching FetchBoondProjectList..." }

        if (event.companyId == null || event.companyId == 0L) {
            logger.warn {
                "Skipping fetch: Invalid companyId (${event.companyId}) for client ${event.clientId}"
            }
            return
        }

        // 1️⃣ Call external system
        val adapterResult = adapter.fetch(event.companyId)

        // 2️⃣ Map adapter result to domain payload
        val mappedPayload =
                adapterResult.projects.map { item ->
                    ListOfProjectsItem(
                            projectId = item.projectId,
                            reference = item.reference,
                            projectTitle = item.projectTitle,
                            projectDescription = item.projectDescription,
                            startDate = item.startDate, // Now supports null/String correctly
                            endDate = item.endDate,
                            forecastEndDate = item.forecastEndDate,
                            status = item.status,
                            manager = item.manager ?: "Unknown Manager"
                    )
                }

        logger.info {
            "Dispatching MarkListOfProjectsFetchedCommand with ${mappedPayload.size} items"
        }

        // 3️⃣ Dispatch command
        commandGateway.send<Any>(
                        MarkListOfProjectsFetchedCommand(
                                clientId = event.clientId,
                                companyId = event.companyId,
                                projectList = mappedPayload
                        )
                )
                .exceptionally { throwable ->
                    logger.error(throwable) {
                        "FAILED to process FetchBoondProjectList: ${throwable.message}"
                    }
                    null
                }
    }
}
