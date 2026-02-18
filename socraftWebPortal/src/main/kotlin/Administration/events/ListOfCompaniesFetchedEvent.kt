package administration.events

import administration.common.Event
import administration.common.ListOfCompaniesItem
import java.util.UUID
import kotlin.collections.List

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822853
*/
data class ListOfCompaniesFetchedEvent(
        var settingsId: UUID,
        var connectionId: UUID?,
        var listOfCompanies: List<ListOfCompaniesItem>
) : Event
