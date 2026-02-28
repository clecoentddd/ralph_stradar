package stradar.platformadministration.domain.commands.defineorganization

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command
import stradar.common.NoArg

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645827750140
*/
@NoArg
data class DefineOrganizationCommand(
        @TargetAggregateIdentifier var organizationId: UUID,
        var personId: UUID,
        var username: String,
        var organizationName: String
) : Command
