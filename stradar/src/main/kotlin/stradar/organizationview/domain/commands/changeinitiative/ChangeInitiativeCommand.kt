package stradar.organizationview.domain.commands.changeinitiative

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661876429645
*/
data class ChangeInitiativeCommand(
    @TargetAggregateIdentifier var initiativeId: UUID,
    var initiativeName: String,
    var organizationId: UUID,
    var status: String
) : Command
