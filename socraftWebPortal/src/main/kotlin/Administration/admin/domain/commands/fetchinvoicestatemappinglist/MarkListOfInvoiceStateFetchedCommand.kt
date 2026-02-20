package administration.admin.domain.commands.fetchinvoicestatemappinglist

import administration.common.Command
import administration.common.ListOfInvoiceStatesItem
import java.util.UUID
import kotlin.collections.List
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713538
*/
data class MarkListOfInvoiceStateFetchedCommand(
        @TargetAggregateIdentifier var settingsId: UUID,
        var connectionId: UUID,
        var listOfInvoiceStates: List<ListOfInvoiceStatesItem>
) : Command
