package stradar.organizationview.createperson.internal

import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.DisallowReplay
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.springframework.stereotype.Component
import stradar.events.OrganizationAdminUserCreatedEvent
import stradar.organizationview.ProcessingGroups
import stradar.organizationview.domain.commands.createperson.CreatePersonCommand
import stradar.support.metadata.*

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661498263330
*/

@ProcessingGroup(ProcessingGroups.ORGANIZATION_VIEW)
@Component
class OrganizationAutomationProcessor(private val commandGateway: CommandGateway) {

  private val logger = KotlinLogging.logger {}

  @DisallowReplay
  @EventHandler
  fun on(event: OrganizationAdminUserCreatedEvent, metadata: MetaData) {

    logger.info { "OrganizationAdminUserCreatedEvent received: $event" }
    logger.info { "Metadata received: $metadata" }
    val actorId = metadata[USER_ID_HEADER] ?: "SYSTEM_AUTO"

    val nextMetadata =
            metadata.and(USER_ID_HEADER, actorId).and(ORGANIZATION_ID_HEADER, event.organizationId)

    commandGateway.send<Any>(
            CreatePersonCommand(
                    personId = event.organizationUserId,
                    auth0UserId = event.auth0UserId,
                    organizationId = event.organizationId,
                    organizationName = event.organizationName,
                    role = event.role,
                    username = event.organizationUserEmail
            ),
            nextMetadata
    )
  }
}
