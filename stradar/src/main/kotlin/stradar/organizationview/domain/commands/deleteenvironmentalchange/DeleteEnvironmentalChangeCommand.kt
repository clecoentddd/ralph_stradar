package stradar.organizationview.domain.commands.deleteenvironmentalchange

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661052478597
*/
data class DeleteEnvironmentalChangeCommand(
    @TargetAggregateIdentifier var environmentalChangeId: UUID,
    var teamId: UUID,
    var organizationId: UUID
) : Command
