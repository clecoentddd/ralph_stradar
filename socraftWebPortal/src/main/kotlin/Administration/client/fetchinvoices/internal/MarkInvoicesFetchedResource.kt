package administration.client.fetchinvoices.internal

import administration.client.domain.commands.fetchinvoices.MarkInvoicesFetchedCommand
import administration.common.ListOfInvoicesItem
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.collections.List
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class FetchInvoicesPayload(
        var companyId: Long,
        var clientId: UUID,
        var invoiceList: List<ListOfInvoicesItem>
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660086962410
*/
@RestController
@RequestMapping("/client")
class MarkInvoicesFetchedResource(private var commandGateway: CommandGateway) {

        var logger = KotlinLogging.logger {}

        @CrossOrigin
        @PostMapping("/debug/fetchinvoices")
        fun processDebugCommand(
                @RequestParam companyId: Long,
                @RequestParam clientId: UUID,
                @RequestParam invoiceList: List<ListOfInvoicesItem>
        ): CompletableFuture<Any> {
                return commandGateway.send(
                        MarkInvoicesFetchedCommand(companyId, clientId, invoiceList)
                )
        }

        @CrossOrigin
        @PostMapping("/fetchinvoices/{id}")
        fun processCommand(
                @PathVariable("id") companyId: Long,
                @RequestBody payload: FetchInvoicesPayload
        ): CompletableFuture<Any> {
                return commandGateway.send(
                        MarkInvoicesFetchedCommand(
                                companyId = payload.companyId,
                                clientId = payload.clientId,
                                invoiceList = payload.invoiceList
                        )
                )
        }
}
