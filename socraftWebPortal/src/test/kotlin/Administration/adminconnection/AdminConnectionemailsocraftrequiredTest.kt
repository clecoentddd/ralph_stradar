package administration.admin.adminconnection

import administration.admin.domain.commands.adminconnection.ToConnectCommand
import administration.common.CommandException
import administration.domain.AdminAccountAggregate
import administration.support.metadata.AdminSecurityHeaders
import java.util.UUID
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Must be a socraft.ch email
 *
 * Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660243006187
 */
class AdminConnectionemailsocraftrequiredTest {

  private lateinit var fixture: FixtureConfiguration<AdminAccountAggregate>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(AdminAccountAggregate::class.java)
  }

  @Test
  fun `Admin Connection email must end with socraft ch`() {
    val connectionId: UUID = UUID.fromString("734400ed-dda8-45ad-9d2f-da00136c3bab")

    // 1. Prepare the command with an invalid email extension
    val command = ToConnectCommand(connectionId = connectionId, email = "test@socraft.com")

    // 2. Wrap it with Metadata to satisfy the Interceptor
    val commandMessage =
            GenericCommandMessage.asCommandMessage<ToConnectCommand>(command)
                    .withMetaData(MetaData.with(AdminSecurityHeaders.SESSION_ID, "test-session"))

    // WHEN & THEN
    fixture.givenNoPriorActivity()
            .`when`(commandMessage) // Send the wrapped message
            // 1. Check for your specific custom exception type
            .expectException(CommandException::class.java)
            // 2. Check that the message matches what you wrote in the 'require' block
            .expectExceptionMessage("Email must be a socraft.ch email")
  }
}
