package stradar.platformadministration.domain.commands.createorganizationadminuser

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764663408532423
*/
data class MarkOrganizationUserAdminCreatedCommand(
        @TargetAggregateIdentifier var organizationId: UUID,
        var organizationUserId: UUID,
        var organizationName: String,
        var role: String,
        var organizationUserEmail: String,
        var auth0UserId: String
) : Command
