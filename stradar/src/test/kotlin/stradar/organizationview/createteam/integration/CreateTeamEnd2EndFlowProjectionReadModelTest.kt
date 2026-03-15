package stradar.organizationview.createteam.integration

import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.GenericQueryMessage
import org.axonframework.queryhandling.QueryGateway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import stradar.common.support.BaseIntegrationTest
import stradar.common.support.awaitUntilAssserted
import stradar.organizationview.domain.commands.createteam.CreateTeamCommand
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand
import stradar.organizationview.teamlist.TeamListByOrganizationQuery
import stradar.organizationview.teamlist.TeamListReadModel
import stradar.support.metadata.*

/** Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661632441150 */
class CreateTeamEnd2EndFlowProjectionReadModelTest : BaseIntegrationTest() {

  @Autowired private lateinit var commandGateway: CommandGateway

  @Autowired private lateinit var queryGateway: QueryGateway

  @Test
  fun `Create Team End2End Flow Projection Read Model Test`() {

    val teamId = UUID.randomUUID()
    val organizationId = UUID.randomUUID()
    val deletedReason = "Reason ABC"

    val metadata =
        MetaData.with(SESSION_ID_HEADER, UUID.randomUUID().toString())
            .and(ORGANIZATION_ID_HEADER, organizationId)
            .and(USER_ID_HEADER, "test-user")

    val createTeamCommand =
        CreateTeamCommand(
            teamId = teamId,
            organizationId = organizationId,
            context = "Context 1",
            level = 3,
            name = "CTO",
            purpose = "Purpose 1")
    commandGateway.sendAndWait<Any>(createTeamCommand, metadata)

    val deleteTeamCommand =
        DeleteTeamCommand(teamId = teamId, organizationId = organizationId, reason = deletedReason)
    commandGateway.sendAndWait<Any>(deleteTeamCommand, metadata)

    awaitUntilAssserted {
      val result =
          queryGateway
              .query(
                  GenericQueryMessage(
                          TeamListByOrganizationQuery(organizationId),
                          ResponseTypes.instanceOf(TeamListReadModel::class.java))
                      .withMetaData(MetaData.with(ORGANIZATION_ID_HEADER, organizationId)),
                  ResponseTypes.instanceOf(TeamListReadModel::class.java))
              .get()
      assertThat(result).isNotNull
      assertThat(result.teams.none { it.teamId == teamId }).isTrue()
      assertThat(result.teams.none { it.reason == deletedReason }).isTrue()
    }
  }
}
