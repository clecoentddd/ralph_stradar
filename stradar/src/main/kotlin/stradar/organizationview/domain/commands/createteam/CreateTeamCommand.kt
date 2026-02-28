package stradar.organizationview.domain.commands.createteam

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645830671029
*/
data class CreateTeamCommand(
        @TargetAggregateIdentifier var teamId: UUID,
        var organizationId: UUID,
        var context: String,
        var level: Int,
        var name: String,
        var purpose: String
) : Command
