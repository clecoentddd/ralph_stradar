package stradar.organizationview.initiativelist.integration

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
import stradar.organizationview.domain.commands.changeinitiativeitem.ChangeInitiativeItemCommand
import stradar.organizationview.domain.commands.createinitiative.CreateInitiativeCommand
import stradar.organizationview.initiativelist.InitiativesReadModel
import stradar.organizationview.initiativelist.InitiativesReadModelQuery
import stradar.support.metadata.*

/**
 *
 * Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661775001214
 */
class InitiativeListUpdateOfAnItemReadModelTest : BaseIntegrationTest() {

        @Autowired private lateinit var commandGateway: CommandGateway

        @Autowired private lateinit var queryGateway: QueryGateway

        @Test
        fun `Initiative List Update Of An Item Read Model Test`() {
                // 1. Setup Data & Metadata
                val initiativeId = UUID.randomUUID()
                val orgId = UUID.randomUUID()
                val strategyId = UUID.randomUUID()
                val teamId = UUID.randomUUID()
                val initiativeName = "Modernize Legacy Infrastructure"
                val diagnosticContent = "This is my diagnostic. So be it."

                val metadata =
                        MetaData.with(USER_ID_HEADER, "test-user")
                                .and("X-Correlation-Id", UUID.randomUUID().toString())
                                .and(SESSION_ID_HEADER, "test-session")
                                .and(ORGANIZATION_ID_HEADER, orgId)

                // 2. Create Initiative
                val createCmd =
                        CreateInitiativeCommand(
                                initiativeId = initiativeId,
                                initiativeName = initiativeName,
                                organizationId = orgId,
                                strategyId = strategyId,
                                teamId = teamId
                        )
                commandGateway.sendAndWait<Any>(createCmd, metadata)

                // 3. Change Diagnostic Item
                val itemId = UUID.randomUUID()
                val changeCmd =
                        ChangeInitiativeItemCommand(
                                initiativeId = initiativeId,
                                step = "DIAGNOSTIC",
                                itemId = itemId,
                                content = diagnosticContent,
                                status = "ACTIVE"
                        )
                commandGateway.sendAndWait<Any>(changeCmd, metadata)

                // 4. Assertions
                awaitUntilAssserted {
                        val result =
                                queryGateway
                                        .query(
                                                GenericQueryMessage(
                                                                InitiativesReadModelQuery(
                                                                        initiativeId
                                                                ),
                                                                ResponseTypes.instanceOf(
                                                                        InitiativesReadModel::class
                                                                                .java
                                                                )
                                                        )
                                                        .withMetaData(
                                                                MetaData.with(
                                                                        ORGANIZATION_ID_HEADER,
                                                                        orgId
                                                                )
                                                        ),
                                                ResponseTypes.instanceOf(
                                                        InitiativesReadModel::class.java
                                                )
                                        )
                                        .get()

                        val entity = result.data

                        // Verify basic fields
                        assertThat(entity.initiativeId).isEqualTo(initiativeId)

                        // Verify the diagnostic items in allItems
                        val diagnosticItems = entity.allItems.filter { it.step == "DIAGNOSTIC" }
                        assertThat(diagnosticItems).hasSize(1)

                        val item = diagnosticItems.first()
                        assertThat(item.id).isEqualTo(itemId)
                        assertThat(item.content).isEqualTo(diagnosticContent)
                        assertThat(item.status).isEqualTo("ACTIVE")
                }
        }
}
