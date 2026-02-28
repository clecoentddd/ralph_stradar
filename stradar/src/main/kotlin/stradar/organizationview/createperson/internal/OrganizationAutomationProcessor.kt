package stradar.organizationview.createperson.internal

import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.DisallowReplay
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.springframework.stereotype.Component
import stradar.events.OrganizationDefinedEvent
import stradar.organizationview.ProcessingGroups
import stradar.organizationview.domain.commands.createperson.CreatePersonCommand

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661498263330
*/

@ProcessingGroup(ProcessingGroups.COMPANY_VIEW)
@Component
class OrganizationAutomationProcessor(private val commandGateway: CommandGateway) {

        private val logger = KotlinLogging.logger {}

        @DisallowReplay
        @EventHandler
        fun on(event: OrganizationDefinedEvent, metadata: MetaData) {

                logger.info { "OrganizationDefinedEvent received: $event" }
                logger.info { "Metadata received: $metadata" }
                val actorId = metadata["x-user-id"] ?: "SYSTEM_AUTO"

                val nextMetadata =
                        MetaData.with("x-user-id", actorId)
                                .and("X-Correlation-Id", metadata["X-Correlation-Id"])

                commandGateway.send<Any>(
                        CreatePersonCommand(
                                personId = event.personId,
                                organizationId = event.organizationId,
                                organizationName = event.organizationName,
                                role = event.role,
                                username = event.username
                        ),
                        nextMetadata
                )
        }
}
