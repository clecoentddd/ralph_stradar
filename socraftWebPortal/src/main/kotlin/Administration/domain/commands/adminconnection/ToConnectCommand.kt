package Administration.domain.commands.adminconnection

import org.axonframework.modelling.command.TargetAggregateIdentifier
import Administration.common.Command
import java.util.UUID;


/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734675975
*/
data class ToConnectCommand(
    @TargetAggregateIdentifier var connectionId:UUID,
	var email:String
): Command
