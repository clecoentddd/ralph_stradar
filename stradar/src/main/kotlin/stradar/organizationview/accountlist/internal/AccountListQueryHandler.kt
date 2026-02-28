package stradar.organizationview.accountlist.internal

import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component
import stradar.organizationview.accountlist.AccountListReadModel
import stradar.organizationview.accountlist.AccountListReadModelEntity
import stradar.organizationview.accountlist.AccountListReadModelQuery
import stradar.organizationview.accountlist.PersonAccountQuery

@Component
class AccountListQueryHandler(private val repository: AccountListReadModelRepository) {

    /** 1. The "List" Query: Returns everything */
    @QueryHandler
    fun handleQuery(query: AccountListReadModelQuery): AccountListReadModel {
        return AccountListReadModel(repository.findAll())
    }

    /** 2. The "Point" Query: Returns one specific person's context */
    @QueryHandler
    fun handlePersonQuery(query: PersonAccountQuery): AccountListReadModelEntity? {
        // We return the Entity directly (or null if not found)
        return repository.findById(query.personId).orElse(null)
    }
}
