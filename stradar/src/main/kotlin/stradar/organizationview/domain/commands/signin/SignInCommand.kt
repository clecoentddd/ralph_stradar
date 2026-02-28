package stradar.organizationview.domain.commands.signin

import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier
import stradar.common.Command

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660937417035
*/
data class SignInCommand(
        @TargetAggregateIdentifier var personId: UUID,
) : Command
