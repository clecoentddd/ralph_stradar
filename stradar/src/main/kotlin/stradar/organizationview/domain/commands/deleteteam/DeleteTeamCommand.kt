package stradar.organizationview.domain.commands.deleteteam

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631612141
*/
data class DeleteTeamCommand(
        @TargetAggregateIdentifier var teamId: UUID,
        var organizationId: UUID,
        var reason: String
) : Command
