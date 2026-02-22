package administration.admin.client.createclientaccount

import administration.client.domain.commands.createclientaccount.CreateAccountCommand
import administration.domain.ClientAccountAggregate
import administration.support.metadata.AppSecurityHeaders
import administration.support.metadata.MetaDataCommandInterceptor
import java.util.UUID
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.matchers.Matchers.exactSequenceOf
import org.axonframework.test.matchers.Matchers.payloadsMatching
import org.hamcrest.CoreMatchers.any
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateClientAccountSecurityDefensiveTest {

        private lateinit var fixture: AggregateTestFixture<ClientAccountAggregate>

        @BeforeEach
        fun setUp() {
                fixture = AggregateTestFixture(ClientAccountAggregate::class.java)

                @Suppress("UNCHECKED_CAST")
                fixture.registerCommandDispatchInterceptor(
                        MetaDataCommandInterceptor() as
                                org.axonframework.messaging.MessageDispatchInterceptor<
                                        org.axonframework.commandhandling.CommandMessage<*>>
                )
        }

        private fun createCommand() =
                CreateAccountCommand(
                        clientId = UUID.randomUUID(),
                        clientEmail = "test@client.ch",
                        companyId = 123L,
                        connectionId = UUID.randomUUID()
                )

        @Test
        fun `Should fail with specific message when Session ID is missing`() {
                val metaData = MetaData.with(AppSecurityHeaders.COMPANY_ID_HEADER, "some-company")

                println("--- TEST START: Should fail when SESSION_ID_HEADER is missing ---")
                println("Action: Sending CreateAccountCommand with COMPANY_ID only...")

                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                fixture.givenNoPriorActivity().`when`(createCommand(), metaData)
                        }

                println("Intercepted: Get exception -> ${exception.message}")

                assertEquals("Missing required header: X-Session-Id", exception.message?.trim())
                println("Result: Test OK - Security blocked the command as expected.")
                println("--- TEST END ---")
        }

        @Test
        fun `Should fail with specific message when Company ID is missing`() {
                val metaData = MetaData.with(AppSecurityHeaders.SESSION_ID_HEADER, "some-session")

                println("--- TEST START: Should fail when COMPANY_ID_HEADER is missing ---")
                println("Action: Sending CreateAccountCommand with SESSION_ID only...")

                val exception =
                        assertThrows(IllegalArgumentException::class.java) {
                                fixture.givenNoPriorActivity().`when`(createCommand(), metaData)
                        }

                println("Intercepted: Get exception -> ${exception.message}")

                assertEquals("Missing required header: X-Company-Id", exception.message?.trim())
                println("Result: Test OK - Security blocked the command as expected.")
                println("--- TEST END ---")
        }

        @Test
        fun `Should pass only when both headers are perfectly present`() {
                val validMetaData =
                        MetaData.with(AppSecurityHeaders.SESSION_ID_HEADER, "valid-session")
                                .and(AppSecurityHeaders.COMPANY_ID_HEADER, "valid-company")

                println("--- TEST START: Should pass when both headers are present ---")
                println("Action: Sending CreateAccountCommand with full security metadata...")

                fixture.givenNoPriorActivity()
                        .`when`(createCommand(), validMetaData)
                        .expectSuccessfulHandlerExecution()
                        .expectEventsMatching(
                                payloadsMatching(exactSequenceOf(any(Any::class.java)))
                        )

                println("Result: Test OK - Command allowed and event published.")
                println("--- TEST END ---")
        }
}
