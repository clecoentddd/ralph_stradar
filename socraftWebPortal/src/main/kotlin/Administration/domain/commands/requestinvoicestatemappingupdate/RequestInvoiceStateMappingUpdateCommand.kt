package administration.domain.commands.requestinvoicestatemappingupdate

import org.axonframework.modelling.command.TargetAggregateIdentifier
import administration.common.Command
import java.util.UUID

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659954713526
*/
data class RequestInvoiceStateMappingUpdateCommand(
    @TargetAggregateIdentifier var settingsId:UUID,
	var connectionId:UUID
): Command
