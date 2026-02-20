package administration.admin.adminconnected.integration

import administration.admin.adminconnected.AdminConnectedReadModel
import administration.admin.adminconnected.AdminConnectedReadModelQuery
import administration.admin.domain.commands.adminconnection.ToConnectCommand
import administration.common.support.BaseIntegrationTest
import administration.common.support.awaitUntilAssserted
import administration.support.metadata.AdminSecurityHeaders // Ensure this is imported
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.GenericCommandMessage // Required for wrapping
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData // Required for headers
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

    val toConnectCommand = ToConnectCommand(connectionId = connectionId, email = testEmail)

    // 1. Prepare the MetaData the interceptor is looking for
    val testMetaData =
            MetaData.with(AdminSecurityHeaders.SESSION_ID, "test-session-id")
                    .and(AdminSecurityHeaders.ADMIN_COMPANY_ID, "test-company-id")

    // 2. Wrap the raw command into a Message with the MetaData
    val commandMessage =
            GenericCommandMessage.asCommandMessage<ToConnectCommand>(toConnectCommand)
                    .withMetaData(testMetaData)

    // 3. Send the command message instead of the raw object
    commandGateway.sendAndWait<Any>(commandMessage)

    awaitUntilAssserted {
      val result =
              queryGateway
                      .query(
                              AdminConnectedReadModelQuery(connectionId),
                              AdminConnectedReadModel::class.java
                      )
                      .join()

      assertThat(result).isNotNull
      assertThat(result.connectionId).isEqualTo(connectionId)
      assertThat(result.email).isEqualTo(testEmail)
    }
  }
}
