package administration.admin.client.createclientaccount

import administration.client.domain.commands.createclientaccount.CreateAccountCommand
import administration.common.CommandException
import administration.common.Event
import administration.common.support.RandomData
import administration.domain.ClientAccountAggregate
import administration.events.AccountCreatedEvent
import java.util.UUID
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Email already exists Boardlink:
 * https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764660313321041
 */
class CreateClientAccountEmailMustBeUniqueTest {

        private lateinit var fixture: FixtureConfiguration<ClientAccountAggregate>

        @BeforeEach
        fun setUp() {
                fixture = AggregateTestFixture(ClientAccountAggregate::class.java)
        }

        @Test
        fun `Create Client Account email must be unique Test`() {

                val clientId: UUID = RandomData.newInstance<UUID> {}
                val duplicateEmail = "client@client.ch"

                // GIVEN: This specific Aggregate ID already has this email in its history
                val events = mutableListOf<Event>()
                events.add(
                        RandomData.newInstance<AccountCreatedEvent> {
                                this.clientId = clientId
                                this.clientEmail = duplicateEmail
                                this.companyId = 799
                                this.connectionId = RandomData.newInstance {}
                        }
                )

                // WHEN: We try to send a command to the SAME ID with the SAME email
                val command =
                        CreateAccountCommand(
                                clientId = clientId, // Use the SAME ID as above
                                clientEmail = duplicateEmail,
                                companyId = 789,
                                connectionId = RandomData.newInstance {}
                        )

                // THEN: The Aggregate sees it already has an email and throws the exception
                fixture.given(events)
                        .`when`(command)
                        .expectException(CommandException::class.java)
                        .expectExceptionMessage(
                                containsString("Account already exists with email: $duplicateEmail")
                        )
        }
}
