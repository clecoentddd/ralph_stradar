package administration.domain.commands.adminconnection

import administration.common.Command
import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734675975
*/
data class ToConnectCommand(@TargetAggregateIdentifier var connectionId: UUID, var email: String) :
    Command
