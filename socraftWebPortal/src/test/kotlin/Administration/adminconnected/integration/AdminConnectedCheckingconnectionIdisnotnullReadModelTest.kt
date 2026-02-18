package administration.adminconnected.integration

import administration.adminconnected.AdminConnectedReadModel
import administration.adminconnected.AdminConnectedReadModelQuery
import administration.common.support.BaseIntegrationTest
import administration.common.support.RandomData
import administration.common.support.awaitUntilAssserted
import administration.domain.commands.adminconnection.ToConnectCommand
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/** Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764659794050479 */
class AdminConnectedCheckingconnectionIdisnotnullReadModelTest : BaseIntegrationTest() {

  @Autowired private lateinit var commandGateway: CommandGateway

  @Autowired private lateinit var queryGateway: QueryGateway

  @Test
  fun `Admin Connected Checkingconnection Idisnotnull Read Model Test`() {

    val connectionId = RandomData.newInstance<UUID> {}

    var toConnectCommand =
        RandomData.newInstance<ToConnectCommand> { this.connectionId = connectionId }

    commandGateway.sendAndWait<Any>(toConnectCommand)

    awaitUntilAssserted {
      var readModel =
          queryGateway.query(
              AdminConnectedReadModelQuery(toConnectCommand.connectionId),
              AdminConnectedReadModel::class.java)
      // TODO add assertions
      assertThat(readModel.get()).isNotNull
    }
  }
}
