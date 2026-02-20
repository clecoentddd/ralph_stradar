package administration.client.domain.commands.clientaccountconnection

import administration.common.Command
import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660036569904
*/
data class ToConnectToAccountCommand(
    var clientEmail: String,
    @TargetAggregateIdentifier var clientId: UUID
) : Command
