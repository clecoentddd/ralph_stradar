package administration.admin.companylistlookup.integration

import administration.admin.companylistlookup.CompanyListLookUpReadModelEntity
import administration.admin.companylistlookup.FetchAllCompaniesQuery
import administration.admin.domain.commands.fetchcompanieslist.MarkListOfCompaniesFetchedCommand
import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.common.SettingsConstants
import administration.common.support.BaseIntegrationTest
import administration.common.support.RandomData
import administration.common.support.awaitUntilAssserted
import administration.support.metadata.AppSecurityHeaders
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CompanyListLookUpBasicCheckWithRealDataTest : BaseIntegrationTest() {

        @Autowired private lateinit var commandGateway: CommandGateway
        @Autowired private lateinit var queryGateway: QueryGateway

        @Test
        fun `Company List Look Up Ensure Projection Updates Read Model Test`() {
                val settingsId = SettingsConstants.SETTINGS_ID

                // Define the metadata required by your interceptor
                val metaData =
                        MetaData.with(AppSecurityHeaders.SESSION_ID_HEADER, "test-session")
                                .and(AppSecurityHeaders.COMPANY_ID_HEADER, "test-company")
                // 1. Initialize Settings
                val createSettingsCommand =
                        RandomData.newInstance<CreateSettingsCommand> {
                                this.settingsId = settingsId
                        }
                // Pass metaData as the second argument
                commandGateway.sendAndWait<Any>(createSettingsCommand, metaData)

                // 2. Fetch Companies
                val markListOfCompaniesFetchedCommand =
                        RandomData.newInstance<MarkListOfCompaniesFetchedCommand> {
                                this.settingsId = settingsId
                        }
                // Pass metaData as the second argument
                commandGateway.sendAndWait<Any>(markListOfCompaniesFetchedCommand, metaData)

                // 3. Verify
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

                        val companiesInDb = result.filter { it.settingsId == settingsId }
                        assertThat(companiesInDb).isNotEmpty

                        val expectedNames =
                                markListOfCompaniesFetchedCommand.listOfCompanies?.map {
                                        it.companyName
                                }
                        val actualNames = companiesInDb.map { it.companyName }
                        assertThat(actualNames).containsAll(expectedNames)
                }
        }
}
