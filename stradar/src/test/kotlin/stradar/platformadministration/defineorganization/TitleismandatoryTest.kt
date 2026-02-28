package stradar.platformadministration.defineorganization

import java.util.UUID
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import stradar.common.CommandException
import stradar.domain.OrganizationAggregate
import stradar.platformadministration.domain.commands.defineorganization.DefineOrganizationCommand

class TitleismandatoryTest {

  private lateinit var fixture: FixtureConfiguration<OrganizationAggregate>
  private lateinit var queryGateway: QueryGateway

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(OrganizationAggregate::class.java)
    queryGateway = mock(QueryGateway::class.java)
    fixture.registerInjectableResource(queryGateway)
  }

  @Test
  fun `Titleismandatory Test`() {
    val organizationId = UUID.randomUUID()
    val personId = UUID.randomUUID()

    val command =
            DefineOrganizationCommand(
                    organizationId = organizationId,
                    personId = personId,
                    organizationName = "",
                    username = "admin"
            )

    fixture.givenNoPriorActivity()
            .`when`(command)
            .expectException(CommandException::class.java)
            .expectExceptionMessage("organizationName is required and cannot be empty")
  }
}
