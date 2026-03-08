package stradar.organizationview.initiativelist.integration

import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryGateway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import stradar.common.support.BaseIntegrationTest
import stradar.common.support.awaitUntilAssserted
import stradar.organizationview.domain.commands.changeinitiativeitem.ChangeInitiativeItemCommand
import stradar.organizationview.domain.commands.createinitiative.CreateInitiativeCommand
import stradar.organizationview.initiativelist.InitiativesReadModel
import stradar.organizationview.initiativelist.InitiativesReadModelQuery
import stradar.support.metadata.ORGANIZATION_ID_HEADER
import stradar.support.metadata.SESSION_ID_HEADER
import stradar.support.metadata.USER_ID_HEADER

/**
 *
 * Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661775112134
 */
class InitiativeListUpdateOfTwoItemsReadModelTest : BaseIntegrationTest() {

        @Autowired private lateinit var commandGateway: CommandGateway

        @Autowired private lateinit var queryGateway: QueryGateway

        @Test
        fun `Initiative List Update Of Two Items Read Model Test`() {
                val initiativeId = UUID.randomUUID()
                val orgId = UUID.randomUUID()
                val strategyId = UUID.randomUUID()
                val teamId = UUID.randomUUID()
                val initiativeName = "Modernize Legacy Infrastructure"

                // 1. Prepare Metadata exactly like your example
                val metadata =
                        MetaData.with(USER_ID_HEADER, "test-user")
                                .and("X-Correlation-Id", UUID.randomUUID().toString())
                                .and(SESSION_ID_HEADER, "test-session")
                                .and(ORGANIZATION_ID_HEADER, orgId)

                // 2. Create Initiative
                val createCommand =
                        CreateInitiativeCommand(
                                initiativeId = initiativeId,
                                initiativeName = initiativeName,
                                organizationId = orgId,
                                strategyId = strategyId,
                                teamId = teamId
                        )
                // Explicitly passing (command, metadata)
                commandGateway.sendAndWait<Any>(createCommand, metadata)

                // 3. Add Diagnostic Item
                val diagCommand =
                        ChangeInitiativeItemCommand(
                                initiativeId = initiativeId,
                                step = "DIAGNOSTIC",
                                itemId = UUID.randomUUID(),
                                content = "This is my diagnostic. So be it.",
                                status = "ACTIVE"
                        )
                commandGateway.sendAndWait<Any>(diagCommand, metadata)

                // 4. Add Overall Approach Item
                val approachCommand =
                        ChangeInitiativeItemCommand(
                                initiativeId = initiativeId,
                                step = "OVERALLAPPROACH",
                                itemId = UUID.randomUUID(),
                                content = "This is my approach. So go for it.",
                                status = "ACTIVE"
                        )
                commandGateway.sendAndWait<Any>(approachCommand, metadata)

                // 5. Assertions
                awaitUntilAssserted {
                        val query = InitiativesReadModelQuery(initiativeId = initiativeId)
                        val response =
                                queryGateway.query(query, InitiativesReadModel::class.java).get()
                        val entity = response.data

                        assertThat(entity).isNotNull
                        assertThat(entity.initiativeName).isEqualTo(initiativeName)

                        // Verify counts in the unified list
                        assertThat(entity.allItems.filter { it.step == "DIAGNOSTIC" }).hasSize(1)
                        assertThat(entity.allItems.filter { it.step == "OVERALLAPPROACH" })
                                .hasSize(1)
                }
        }
}
