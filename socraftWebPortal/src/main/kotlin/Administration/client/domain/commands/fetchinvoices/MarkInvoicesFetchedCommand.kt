package administration.client.domain.commands.fetchinvoices

import administration.common.Command
import administration.common.ListOfInvoicesItem
import java.util.UUID
import kotlin.collections.List
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660086962410
*/
data class MarkInvoicesFetchedCommand(
        @TargetAggregateIdentifier var companyId: Long,
        var clientId: UUID,
        var invoiceList: List<ListOfInvoicesItem>
) : Command
