package stradar.organizationview.domain.commands.createinitiative

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645854569036
*/
data class CreateInitiativeCommand(
    @TargetAggregateIdentifier var initiativeId: UUID,
    var initiativeName: String,
    var organizationId: UUID,
    var strategyId: UUID,
    var teamId: UUID
) : Command
