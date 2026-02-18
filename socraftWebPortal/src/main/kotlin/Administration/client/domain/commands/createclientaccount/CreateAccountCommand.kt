package administration.client.domain.commands.createclientaccount

import administration.common.Command
import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660029388833
*/
data class CreateAccountCommand(
        @TargetAggregateIdentifier var clientId: UUID,
        var clientEmail: String,
        var companyId: Long,
        var connectionId: UUID,
) : Command
