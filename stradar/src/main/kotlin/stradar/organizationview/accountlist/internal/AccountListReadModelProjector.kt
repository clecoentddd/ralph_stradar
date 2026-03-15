package stradar.organizationview.accountlist.internal

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import stradar.common.resolveOrganizationId
import stradar.events.PersonCreatedEvent
import stradar.organizationview.ProcessingGroups
import stradar.organizationview.accountlist.AccountListReadModelEntity

interface AccountListReadModelRepository : JpaRepository<AccountListReadModelEntity, UUID> {
  fun existsByUsername(username: String): Boolean
  fun findByAuth0UserId(auth0UserId: String): AccountListReadModelEntity?
}

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764645830935933
*/
@Component
@ProcessingGroup(ProcessingGroups.ORGANIZATION_VIEW)
class AccountListReadModelProjector(private val repository: AccountListReadModelRepository) {

  private val logger = KotlinLogging.logger {}

  @EventHandler
  fun on(event: PersonCreatedEvent, metaData: MetaData) {
    logger.info { "Projecting PersonCreatedEvent for: ${event.organizationName}" }
    val secureOrgId = metaData.resolveOrganizationId()
    val entity = repository.findById(event.personId).orElseGet { AccountListReadModelEntity() }
    entity
            .apply {
              organizationId = secureOrgId
              organizationName = event.organizationName
              personId = event.personId
              username = event.username
              role = event.role
              auth0UserId = event.auth0UserId
            }
            .also { repository.save(it) }
  }
}
