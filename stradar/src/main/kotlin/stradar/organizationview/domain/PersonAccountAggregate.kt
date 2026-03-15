package stradar.domain

import java.util.UUID
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import stradar.events.PersonCreatedEvent
import stradar.events.PersonSignedInEvent
import stradar.organizationview.domain.commands.createperson.CreatePersonCommand
import stradar.organizationview.domain.commands.signin.SignInCommand
import stradar.support.metadata.USER_ID_HEADER

@Aggregate
class PersonAccountAggregate {

  private val logger = KotlinLogging.logger {}

  @AggregateIdentifier private var personId: UUID? = null

  private var auth0UserId: String? = null
  private var organizationId: UUID? = null
  private var organizationName: String? = null
  private var role: String? = null
  private var username: String? = null

  constructor()

  // ------------------------------------------------------------
  // CREATE PERSON
  // ------------------------------------------------------------
  @CommandHandler
  constructor(command: CreatePersonCommand, metadata: MetaData) : this() {
    val creatorPersonId = metadata[USER_ID_HEADER]?.toString()

    logger.info { "Creating Person: ${command.personId} (Creator: $creatorPersonId)" }

    AggregateLifecycle.apply(
        PersonCreatedEvent(
            personId = command.personId,
            auth0UserId = command.auth0UserId,
            organizationId = command.organizationId,
            organizationName = command.organizationName,
            role = command.role,
            username = command.username))
  }

  // ------------------------------------------------------------
  // SIGN IN
  // ------------------------------------------------------------
  @CommandHandler
  fun handle(command: SignInCommand, metadata: MetaData) {
    val sessionPersonId = metadata[USER_ID_HEADER]?.toString()

    if (sessionPersonId != null && sessionPersonId != command.personId.toString()) {
      throw IllegalAccessException("Security Error: Identity mismatch.")
    }

    val currentOrgId = organizationId ?: throw IllegalStateException("No Organization assigned.")

    val currentRole = role ?: throw IllegalStateException("No Role assigned.")

    AggregateLifecycle.apply(
        PersonSignedInEvent(
            personId = command.personId, organizationId = currentOrgId, role = currentRole))
  }

  // ------------------------------------------------------------
  // EVENT SOURCING
  // ------------------------------------------------------------
  @EventSourcingHandler
  fun on(event: PersonCreatedEvent) {
    personId = event.personId
    auth0UserId = event.auth0UserId
    organizationId = event.organizationId
    organizationName = event.organizationName
    role = event.role
    username = event.username
  }

  @EventSourcingHandler
  fun on(event: PersonSignedInEvent) {
    personId = event.personId
  }
}
