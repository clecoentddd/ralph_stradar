package stradar.organizationview.domain.commands.changeinitiativeitem

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661765153723
*/
data class ChangeInitiativeItemCommand(
        @TargetAggregateIdentifier var initiativeId: UUID,
        var step: String,
        var itemId: UUID,
        var content: String,
        var status: String
) : Command
