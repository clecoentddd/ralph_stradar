package stradar.platformadministration.domain.commands.signinadmin

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660815651079
*/
data class SignInAdminCommand(
    @TargetAggregateIdentifier var adminAccountId: UUID,
    var username: String
) : Command
