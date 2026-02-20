package administration.events

import administration.common.Event
import administration.common.ProjectDetails
import java.util.UUID
import kotlin.collections.List

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660044053419
*/
data class ListOfProjectsFetchedEvent(
        var companyId: Long,
        var clientId: UUID,
        var projectList: List<ProjectDetails>
) : Event
