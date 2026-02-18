package administration.client.domain.commands.fetchorders

import administration.common.Command
import administration.common.ListOfOrdersItem
import java.util.UUID
import kotlin.collections.List
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660085256081
*/
data class MarkOrdersFetchedCommand(
        @TargetAggregateIdentifier var companyId: Long,
        var clientId: UUID,
        var orderList: List<ListOfOrdersItem>
) : Command
