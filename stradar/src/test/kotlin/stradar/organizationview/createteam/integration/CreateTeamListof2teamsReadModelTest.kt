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
import stradar.organizationview.teamlist.TeamListByOrganizationQuery
import stradar.organizationview.teamlist.TeamListReadModel
import stradar.support.metadata.*

class CreateTeamListof2teamsReadModelTest : BaseIntegrationTest() {

  @Autowired private lateinit var commandGateway: CommandGateway
  @Autowired private lateinit var queryGateway: QueryGateway

  @Test
  fun `Create Team Listof2teams Read Model Test`() {
    val orgId = UUID.fromString("00000000-0000-0000-0000-000000000456")
    val teamId1 = UUID.fromString("00000000-0000-0000-0000-000000000123")
    val teamId2 = UUID.fromString("00000000-0000-0000-0000-000000000124")

    // 🛡️ 1. Prepare Metadata to satisfy the Interceptor/Header requirement
    val testMeta =
        MetaData.with(SESSION_ID_HEADER, "test-session-123")
            .and(USER_ID_HEADER, "test-admin")
            .and("X-Correlation-Id", UUID.randomUUID().toString())
            .and(ORGANIZATION_ID_HEADER, orgId)

    // 🚀 2. Send First Command with Metadata
    val createTeam1 =
        CreateTeamCommand(
            teamId = teamId1,
            organizationId = orgId,
            context = "Context 1",
            level = 3,
            name = "CTO",
            purpose = "Purpose 1")
    commandGateway.sendAndWait<Any>(createTeam1, testMeta)

    // 🚀 3. Send Second Command with Metadata
    val createTeam2 =
        CreateTeamCommand(
            teamId = teamId2,
            organizationId = orgId,
            context = "Context 2",
            level = 4,
            name = "Frontend Lead",
            purpose = "UI Development")
    commandGateway.sendAndWait<Any>(createTeam2, testMeta)

    // ✅ 4. Assert the Read Model has exactly 2 items
    awaitUntilAssserted {
      val result =
          queryGateway
              .query(
                  GenericQueryMessage(
                          TeamListByOrganizationQuery(orgId),
                          ResponseTypes.instanceOf(TeamListReadModel::class.java))
                      .withMetaData(MetaData.with("organizationId", orgId)),
                  ResponseTypes.instanceOf(TeamListReadModel::class.java))
              .get()

      assertThat(result.teams).hasSize(2)

      val ids = result.teams.map { it.teamId }
      assertThat(ids).containsExactlyInAnyOrder(teamId1, teamId2)

      val names = result.teams.map { it.name }
      assertThat(names).containsExactlyInAnyOrder("CTO", "Frontend Lead")

      val levels = result.teams.map { it.level }
      assertThat(levels).contains(3, 4)
    }
  }
}
