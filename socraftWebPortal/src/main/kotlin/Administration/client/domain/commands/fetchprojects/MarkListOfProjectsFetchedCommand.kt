package administration.client.domain.commands.fetchprojects

import administration.common.Command
import administration.common.ProjectDetails
import java.util.UUID
import kotlin.collections.List
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660044053423
*/
data class MarkListOfProjectsFetchedCommand(
        var clientId: UUID,
        @TargetAggregateIdentifier var companyId: Long,
        var projectList: List<ProjectDetails>
) : Command
