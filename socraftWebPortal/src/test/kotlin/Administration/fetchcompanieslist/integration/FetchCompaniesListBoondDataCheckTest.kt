package administration.fetchcompanieslist.integration

import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.admin.domain.commands.requestcompanylistupdate.RequestCompanyListUpdateCommand
import administration.common.support.BaseIntegrationTest
import administration.common.support.RandomData
import administration.common.support.StreamAssertions
import administration.common.support.awaitUntilAssserted
import administration.events.ListOfCompaniesFetchedEvent
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import java.util.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

class FetchCompaniesListBoondDataCheckTest : BaseIntegrationTest() {

    @Autowired private lateinit var commandGateway: CommandGateway
    @Autowired private lateinit var streamAssertions: StreamAssertions

    companion object {
        // 1. Define the property ONCE without @JvmStatic
        private val wireMockServer =
                WireMockServer(wireMockConfig().port(8089).usingFilesUnderClasspath("wiremock"))

        // 2. Place @BeforeAll and @JvmStatic on the FUNCTION
        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            wireMockServer.start()
            WireMock.configureFor("localhost", 8089)
        }

        // 3. Place @AfterAll and @JvmStatic on the FUNCTION
        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            wireMockServer.stop()
        }
    }

    @BeforeEach
    fun clearMappings() {
        WireMock.reset() // Clears stubs between test runs if needed
    }

    @Test
    fun `Fetch Companies List Boond Data Check Test`() {
        // 1. THE STUB
        stubFor(
                get(urlEqualTo("/api/v1/companies"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        // WireMock automatically looks in
                                        // src/test/resources/wiremock/__files/
                                        .withBodyFile("administration/companies.json")
                        )
        )

        // 2. Setup IDs
        val settingsId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val connectionId = UUID.randomUUID()

        // 3. Create Settings
        val createSettingsCommand =
                RandomData.newInstance<CreateSettingsCommand> {
                    this.settingsId = settingsId
                    this.connectionId = connectionId
                }
        commandGateway.sendAndWait<Any>(createSettingsCommand)

        // 4. Request Update
        val requestCompanyListUpdateCommand =
                RandomData.newInstance<RequestCompanyListUpdateCommand> {
                    this.settingsId = settingsId
                }
        commandGateway.sendAndWait<Any>(requestCompanyListUpdateCommand)

        // 5. Verify Event
        awaitUntilAssserted {
            streamAssertions.assertEvent(settingsId.toString()) { event ->
                // 1. Check if the event is the right type first
                if (event is ListOfCompaniesFetchedEvent) {
                    // 2. Now it's safe to check properties
                    event.listOfCompanies.size == 3 &&
                            event.listOfCompanies.any { it.companyName == "OSCIN SARL" }
                } else {
                    // 3. If it's a different event (like SettingsCreatedEvent), ignore it
                    false
                }
            }
        }

        // Deep assertion
        awaitUntilAssserted {
            streamAssertions.assertEvent(settingsId.toString()) { event ->
                // Use 'is' to safely check the type and enable smart casting
                event is ListOfCompaniesFetchedEvent &&
                        event.listOfCompanies.size == 3 &&
                        event.listOfCompanies.any { it.companyName == "OSCIN SARL" }
            }
        }
    }
}
