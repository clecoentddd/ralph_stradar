package administration.admin.domain.commands.requestinvoicestatemappingupdate

import administration.common.Command
import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713526
*/
data class RequestInvoiceStateMappingUpdateCommand(
        @TargetAggregateIdentifier var settingsId: UUID,
        var connectionId: UUID
) : Command
