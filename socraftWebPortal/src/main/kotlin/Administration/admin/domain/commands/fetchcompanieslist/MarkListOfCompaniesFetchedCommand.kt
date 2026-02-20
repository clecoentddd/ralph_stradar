package administration.admin.domain.commands.fetchcompanieslist

import administration.common.Command
import administration.common.CompanyDetails
import java.util.UUID
import kotlin.collections.List
import org.axonframework.modelling.command.TargetAggregateIdentifier

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659734822856
*/
data class MarkListOfCompaniesFetchedCommand(
        @TargetAggregateIdentifier var settingsId: UUID,
        var connectionId: UUID,
        var listOfCompanies: List<CompanyDetails>
) : Command
