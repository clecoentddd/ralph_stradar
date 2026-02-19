package administration.adminconnected.integration

import administration.adminconnected.AdminConnectedReadModel
import administration.adminconnected.AdminConnectedReadModelQuery
import administration.common.support.BaseIntegrationTest
import administration.common.support.awaitUntilAssserted
import administration.domain.commands.adminconnection.ToConnectCommand
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AdminConnectedCheckingconnectionIdisnotnullReadModelTest : BaseIntegrationTest() {

  @Autowired lateinit var commandGateway: CommandGateway
  @Autowired lateinit var queryGateway: QueryGateway

  @Test
  fun `Admin Connected Read Model Test`() {
    val connectionId = UUID.randomUUID()
    val testEmail = "test@socraft.ch"

    // FIX: Call the constructor directly instead of using RandomData's setter block
    val toConnectCommand = ToConnectCommand(connectionId = connectionId, email = "test@socraft.ch")

    // Now send the command
    commandGateway.sendAndWait<Any>(toConnectCommand)

    awaitUntilAssserted {
      val result =
              queryGateway
                      .query(
                              AdminConnectedReadModelQuery(connectionId),
                              AdminConnectedReadModel::class.java
                      )
                      .join()

      // 1. Assert the result itself is not null first
      assertThat(result).isNotNull

      // 2. Now it's safe to check the data inside
      assertThat(result.connectionId).isEqualTo(connectionId)
      assertThat(result.email).isEqualTo(testEmail)
    }
  }
}
