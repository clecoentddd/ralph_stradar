package stradar.domain

import java.util.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.*
import org.axonframework.spring.stereotype.Aggregate
import stradar.common.CommandException
import stradar.common.CommandResult
import stradar.events.OrganizationAdminUserCreatedEvent
import stradar.events.OrganizationDefinedEvent
import stradar.platformadministration.domain.commands.createorganizationadminuser.MarkOrganizationUserAdminCreatedCommand
import stradar.platformadministration.domain.commands.defineorganization.DefineOrganizationCommand

@Aggregate
class OrganizationAggregate() {

  @AggregateIdentifier private var organizationId: UUID? = null

  // Keep track of all admin users in the organization
  @AggregateMember private var adminUsers: MutableSet<AdminUserEntity> = mutableSetOf()

  // ------------------------------------------------
  // ORGANIZATION CREATION
  // ------------------------------------------------

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: DefineOrganizationCommand): CommandResult {

    if (command.organizationName.isBlank()) {
      throw CommandException("organizationName is required")
    }

    AggregateLifecycle.apply(
            OrganizationDefinedEvent(
                    organizationId = command.organizationId,
                    organizationName = command.organizationName,
                    organizationUserEmail = command.organizationUserEmail,
                    organizationUserId = command.organizationUserId,
                    role = command.role
            )
    )

    return CommandResult(command.organizationId, AggregateLifecycle.getVersion())
  }

  @EventSourcingHandler
  fun on(event: OrganizationDefinedEvent) {
    organizationId = event.organizationId
  }

  // ------------------------------------------------
  // ADMIN USER CREATION
  // ------------------------------------------------

  @CommandHandler
  fun handle(command: MarkOrganizationUserAdminCreatedCommand): CommandResult {

    if (adminUsers.any { it.organizationUserId == command.organizationUserId }) {
      throw CommandException("Admin user already exists")
    }

    AggregateLifecycle.apply(
            OrganizationAdminUserCreatedEvent(
                    organizationId = command.organizationId,
                    organizationUserId = command.organizationUserId,
                    organizationName = command.organizationName,
                    role = command.role,
                    organizationUserEmail = command.organizationUserEmail,
                    auth0UserId = command.auth0UserId
            )
    )

    return CommandResult(command.organizationId, AggregateLifecycle.getVersion())
  }

  @EventSourcingHandler
  fun on(event: OrganizationAdminUserCreatedEvent) {

    adminUsers.add(
            AdminUserEntity(
                    organizationUserId = event.organizationUserId,
                    organizationUserEmail = event.organizationUserEmail,
                    role = event.role,
                    auth0UserId = event.auth0UserId
            )
    )
  }
}

// -------------------
// Admin user entity inside the aggregate
// -------------------

class AdminUserEntity(
        @EntityId val organizationUserId: UUID, // stable domain ID
        var organizationUserEmail: String, // email or username
        var role: String, // e.g. "admin"
        var auth0UserId: String // Auth0 ID (String, not UUID)
)
