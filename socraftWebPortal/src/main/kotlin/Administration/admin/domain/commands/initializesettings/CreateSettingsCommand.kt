package administration.admin.domain.commands.initializesettings

import administration.common.Command
import java.util.UUID
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659756306800
*/
data class CreateSettingsCommand(
        @TargetAggregateIdentifier var settingsId: UUID,
        var connectionId: UUID
) : Command
