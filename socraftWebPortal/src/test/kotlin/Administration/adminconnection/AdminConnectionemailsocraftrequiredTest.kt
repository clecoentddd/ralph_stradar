package administration.adminconnection

import administration.common.CommandException
import administration.domain.AdminAccountAggregate
import administration.domain.commands.adminconnection.ToConnectCommand
import java.util.UUID
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

        // GIVEN: Using an invalid email extension (.com instead of .ch)
        val command = ToConnectCommand(connectionId = connectionId, email = "test@socraft.com")

        // WHEN & THEN
        fixture.givenNoPriorActivity()
                .`when`(command)
                // 1. Check for your specific custom exception type
                .expectException(CommandException::class.java)
                // 2. Check that the message matches what you wrote in the 'require' block
                .expectExceptionMessage("Email must be a socraft.ch email")
    }
}
