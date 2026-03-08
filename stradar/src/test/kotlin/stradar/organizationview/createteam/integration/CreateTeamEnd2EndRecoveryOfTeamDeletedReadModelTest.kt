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
import stradar.common.support.RandomData
import stradar.common.support.awaitUntilAssserted
import stradar.organizationview.domain.commands.createteam.CreateTeamCommand
import stradar.organizationview.domain.commands.deleteteam.DeleteTeamCommand
import stradar.organizationview.domain.commands.updateteam.UpdateTeamCommand
import stradar.organizationview.teamlist.TeamListByOrganizationQuery
import stradar.organizationview.teamlist.TeamListReadModel

class CreateTeamEnd2EndRecoveryOfTeamDeletedReadModelTest : BaseIntegrationTest() {

        @Autowired private lateinit var commandGateway: CommandGateway
        @Autowired private lateinit var queryGateway: QueryGateway

        @Test
        fun `Create Team End2End Recovery Of Team Deleted Read Model Test`() {
                // 1. Generate valid random UUIDs for this specific test run
                val teamId = RandomData.newInstance<UUID> {}
                val organizationId = RandomData.newInstance<UUID> {}
                val deletedReason = "Reason ABC"

                // 2. Prepare required MetaData
                val meta =
                        MetaData.with("X-Session-Id", "test-session")
                                .and("x-user-id", "test-admin")
                                .and("organizationId", organizationId)

                // 3. Create the team
                val createTeamCommand =
                        CreateTeamCommand(
                                teamId = teamId,
                                organizationId = organizationId,
                                context = "Context 1",
                                level = 3,
                                name = "CTO",
                                purpose = "Purpose 1"
                        )
                commandGateway.sendAndWait<Any>(createTeamCommand, meta)

                // 4. Delete the team
                val deleteTeamCommand =
                        DeleteTeamCommand(
                                teamId = teamId,
                                organizationId = organizationId,
                                reason = deletedReason
                        )
                commandGateway.sendAndWait<Any>(deleteTeamCommand, meta)

                // 5. Update (Recover) the team
                val updateTeamCommand =
                        UpdateTeamCommand(
                                teamId = teamId,
                                organizationId = organizationId,
                                context = "Context 1",
                                level = 4,
                                name = "CTO v2",
                                purpose = "Purpose 1"
                        )
                commandGateway.sendAndWait<Any>(updateTeamCommand, meta)

                // 6. Wait for the Read Model to reflect the recovery
                awaitUntilAssserted {
                        val result =
                                queryGateway
                                        .query(
                                                GenericQueryMessage(
                                                                TeamListByOrganizationQuery(
                                                                        organizationId
                                                                ),
                                                                ResponseTypes.instanceOf(
                                                                        TeamListReadModel::class
                                                                                .java
                                                                )
                                                        )
                                                        .withMetaData(
                                                                MetaData.with(
                                                                        "organizationId",
                                                                        organizationId
                                                                )
                                                        ),
                                                ResponseTypes.instanceOf(
                                                        TeamListReadModel::class.java
                                                )
                                        )
                                        .get()

                        // result.teams is the List<TeamListReadModelEntity>
                        val recoveredTeam = result.teams.find { it.teamId == teamId }

                        assertThat(recoveredTeam).isNotNull
                        assertThat(recoveredTeam?.context).isEqualTo("Context 1")
                        assertThat(recoveredTeam?.level).isEqualTo(4)
                        assertThat(recoveredTeam?.name).isEqualTo("CTO v2")
                        assertThat(recoveredTeam?.organizationId).isEqualTo(organizationId)
                        assertThat(recoveredTeam?.purpose).isEqualTo("Purpose 1")
                }
        }
}
