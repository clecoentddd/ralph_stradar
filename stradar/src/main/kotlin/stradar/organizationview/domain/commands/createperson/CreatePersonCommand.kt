package stradar.organizationview.domain.commands.createperson

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661498263339
*/
data class CreatePersonCommand(
        @TargetAggregateIdentifier var personId: UUID,
        var organizationId: UUID,
        var organizationName: String,
        var role: String,
        var username: String
) : Command
