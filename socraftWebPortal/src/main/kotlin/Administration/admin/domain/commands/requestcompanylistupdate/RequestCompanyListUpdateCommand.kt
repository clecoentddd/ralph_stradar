package administration.admin.domain.commands.requestcompanylistupdate

import administration.common.Command
import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822177
*/
data class RequestCompanyListUpdateCommand(
        var connectionId: UUID,
        @TargetAggregateIdentifier var settingsId: UUID
) : Command
