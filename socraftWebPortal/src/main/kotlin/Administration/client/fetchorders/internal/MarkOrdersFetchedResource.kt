package administration.client.fetchorders.internal

import administration.client.domain.commands.fetchorders.MarkOrdersFetchedCommand
import administration.common.ListOfOrdersItem
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.collections.List
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class FetchordersPayload(
        var companyId: Long,
        var clientId: UUID,
        var orderList: List<ListOfOrdersItem>
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660085256081
*/
@RestController
class MarkOrdersFetchedResource(private var commandGateway: CommandGateway) {

        var logger = KotlinLogging.logger {}

        @CrossOrigin
        @PostMapping("/debug/fetchorders")
        fun processDebugCommand(
                @RequestParam companyId: Long,
                @RequestParam clientId: UUID,
                @RequestParam orderList: List<ListOfOrdersItem>
        ): CompletableFuture<Any> {
                return commandGateway.send(MarkOrdersFetchedCommand(companyId, clientId, orderList))
        }

        @CrossOrigin
        @PostMapping("/fetchorders/{id}")
        fun processCommand(
                @PathVariable("id") companyId: Long,
                @RequestBody payload: FetchordersPayload
        ): CompletableFuture<Any> {
                return commandGateway.send(
                        MarkOrdersFetchedCommand(
                                companyId = payload.companyId,
                                clientId = payload.clientId,
                                orderList = payload.orderList
                        )
                )
        }
}
