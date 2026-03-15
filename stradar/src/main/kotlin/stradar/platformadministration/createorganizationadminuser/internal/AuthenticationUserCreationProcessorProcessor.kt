package stradar.platformadministration.createorganizationadminuser.internal

import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import stradar.common.Processor
import stradar.events.OrganizationDefinedEvent
import stradar.platformadministration.createorganizationadminuser.internal.adapter.AuthenticationService
import stradar.platformadministration.domain.commands.createorganizationadminuser.MarkOrganizationUserAdminCreatedCommand

@Component
class AuthenticationUserCreationProcessor(
        private val commandGateway: CommandGateway,
        private val authenticationService: AuthenticationService
) : Processor {

  private val logger = KotlinLogging.logger {}

  // AuthenticationUserCreationProcessor.kt

  @EventHandler
  fun on(event: OrganizationDefinedEvent) {
    logger.info {
      "OrganizationDefinedEvent received — orgId: ${event.organizationId}, user: ${event.organizationUserEmail}"
    }

    val auth0UserId =
            try {
              authenticationService.createUser(
                      event.organizationUserEmail,
                      event.organizationId.toString()
              )
            } catch (ex: Exception) {
              logger.error(ex) {
                "Auth0 user creation failed for org ${event.organizationId} / user ${event.organizationUserEmail} — aborting event handling"
              }
              throw ex // <-- rethrow so Axon marks this event handler as failed
            }

    logger.info { "Auth0 user created: $auth0UserId — proceeding to send command" }

    commandGateway.send<Any>(
            MarkOrganizationUserAdminCreatedCommand(
                    organizationId = event.organizationId,
                    organizationUserId = event.organizationUserId,
                    organizationName = event.organizationName,
                    role = event.role,
                    organizationUserEmail = event.organizationUserEmail,
                    auth0UserId = auth0UserId
            )
    )

    logger.info { "MarkOrganizationUserAdminCreatedCommand sent for org ${event.organizationId}" }
  }
}
