package administration.admin.companylistlookup.integration

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity
import administration.admin.companylistlookup.FetchAllCompaniesQuery
import administration.admin.domain.commands.fetchcompanieslist.MarkListOfCompaniesFetchedCommand
import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.common.CompanyDetails
import administration.common.SettingsConstants
import administration.common.support.BaseIntegrationTest
import administration.common.support.RandomData
import administration.common.support.awaitUntilAssserted
import administration.support.metadata.AdminSecurityHeaders
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CompanyListLookUpEnsureProjectionUpdatesReadModelTest : BaseIntegrationTest() {

        @Autowired private lateinit var commandGateway: CommandGateway
        @Autowired private lateinit var queryGateway: QueryGateway

        @Test
        fun `Company List Look Up Ensure Projection Updates Read Model Test`() {
                val settingsId = SettingsConstants.SETTINGS_ID
                val metaData = MetaData.with(AdminSecurityHeaders.SESSION_ID, "test-session")
                val targetConnectionId = UUID.fromString("24af641b-d7ef-43ce-8325-79089244a4a8")

                // 1. Initialize Settings
                val createSettingsCommand =
                        RandomData.newInstance<CreateSettingsCommand> {
                                this.settingsId = settingsId
                                this.connectionId = UUID.randomUUID()
                        }
                commandGateway.sendAndWait<Any>(createSettingsCommand, metaData)

                // 2. First Fetch (3 companies)
                val command1 =
                        RandomData.newInstance<MarkListOfCompaniesFetchedCommand> {
                                this.settingsId = settingsId
                                this.connectionId = UUID.randomUUID()
                                // Make sure the property name matches your Command class (usually
                                // listOfCompanies)
                                this.listOfCompanies =
                                        listOf(
                                                CompanyDetails(
                                                        companyId = 789,
                                                        companyName = "OSCIN SARL"
                                                ),
                                                CompanyDetails(
                                                        companyId = 790,
                                                        companyName = "Blanc SA"
                                                ),
                                                CompanyDetails(
                                                        companyId = 791,
                                                        companyName = "TechStart SAS"
                                                )
                                        )
                        }
                commandGateway.sendAndWait<Any>(command1, metaData)

                // 3. Second Fetch (Overwrites/Updates to 2 companies)
                val command2 =
                        RandomData.newInstance<MarkListOfCompaniesFetchedCommand> {
                                this.settingsId = settingsId
                                this.connectionId = targetConnectionId
                                this.listOfCompanies =
                                        listOf(
                                                CompanyDetails(
                                                        companyId = 789,
                                                        companyName = "OSCIN SARL"
                                                ),
                                                CompanyDetails(
                                                        companyId = 790,
                                                        companyName = "Blanc SA"
                                                )
                                        )
                        }
                commandGateway.sendAndWait<Any>(command2, metaData)

                // 4. Verify the flat rows in the DB
                awaitUntilAssserted {
                        val result =
                                queryGateway
                                        .query(
                                                FetchAllCompaniesQuery(),
                                                ResponseTypes.multipleInstancesOf(
                                                        CompanyListLookUpReadModelEntity::class.java
                                                )
                                        )
                                        .get()

                        val rows = result.filter { it.settingsId == settingsId }

                        // This is the key: if the first event is processed but not the second,
                        // this assertion will FAIL (size is 3), and awaitUntilAssserted will RETRY.
                        assertThat(rows)
                                .`as`("Waiting for the second event to overwrite the first")
                                .hasSize(2)

                        // Also verify the connectionId matches the SECOND command
                        assertThat(rows.all { it.connectionId == targetConnectionId }).isTrue()

                        // Verify the names are exactly what we expected from the second list
                        assertThat(rows.map { it.companyName })
                                .containsExactlyInAnyOrder("OSCIN SARL", "Blanc SA")
                }
        }
}
