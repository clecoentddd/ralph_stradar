package administration.fetchinvoicestatemappinglist.internal

import administration.common.ListOfInvoiceStatesItem
import administration.common.SettingsConstants // Fixed: Ensure this is imported
import administration.domain.commands.fetchinvoicestatemappinglist.MarkListOfInvoiceStateFetchedCommand
import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class FetchInvoiceStateMappingListPayload(
        var connectionId: UUID,
        var listOfInvoiceStates: List<ListOfInvoiceStatesItem>
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713538
*/
@RestController
class MarkListOfInvoiceStateFetchedResource(private var commandGateway: CommandGateway) {

    private val logger = KotlinLogging.logger {}

    @CrossOrigin
    @PostMapping("/debug/fetchinvoicestatemappinglist")
    fun processDebugCommand(
            @RequestParam connectionId: UUID,
            @RequestBody listOfInvoiceStates: List<ListOfInvoiceStatesItem>
    ): CompletableFuture<Any> {
        return commandGateway.send(
                MarkListOfInvoiceStateFetchedCommand(
                        settingsId = SettingsConstants.SETTINGS_ID, // Use fixed Constant
                        connectionId = connectionId,
                        listOfInvoiceStates = listOfInvoiceStates
                )
        )
    }

    @CrossOrigin
    @PostMapping("/fetchinvoicestatemappinglist/{id}")
    fun processCommand(
            @PathVariable("id") id: String,
            @RequestBody payload: FetchInvoiceStateMappingListPayload
    ): CompletableFuture<Any> {
        return commandGateway.send(
                MarkListOfInvoiceStateFetchedCommand(
                        settingsId = SettingsConstants.SETTINGS_ID,
                        connectionId = payload.connectionId,
                        listOfInvoiceStates = payload.listOfInvoiceStates
                )
        )
    }
}
