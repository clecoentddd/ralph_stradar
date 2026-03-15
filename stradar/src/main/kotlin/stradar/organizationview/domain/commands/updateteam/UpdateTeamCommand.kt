package stradar.organizationview.domain.commands.updateteam

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661631265233
*/
data class UpdateTeamCommand(
    @TargetAggregateIdentifier var teamId: UUID,
    var context: String,
    var level: Int,
    var name: String,
    var organizationId: UUID,
    var purpose: String
) : Command
