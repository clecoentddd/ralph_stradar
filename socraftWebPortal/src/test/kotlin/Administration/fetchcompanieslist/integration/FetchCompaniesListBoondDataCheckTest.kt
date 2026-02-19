package administration.fetchcompanieslist.integration

import administration.common.support.BaseIntegrationTest
import administration.common.support.RandomData
import administration.common.support.StreamAssertions
import administration.common.support.awaitUntilAssserted
import administration.domain.commands.initializesettings.CreateSettingsCommand
import administration.domain.commands.requestcompanylistupdate.RequestCompanyListUpdateCommand
import administration.events.ListOfCompaniesFetchedEvent
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

class FetchCompaniesListBoondDataCheckTest : BaseIntegrationTest() {

    @Autowired private lateinit var commandGateway: CommandGateway
    @Autowired private lateinit var streamAssertions: StreamAssertions

    companion object {
        // Manually start WireMock on port 8089
        private val wireMockServer = WireMockServer(wireMockConfig().port(8089))

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            wireMockServer.start()
            // Configure the static client to talk to our manual server
            WireMock.configureFor("localhost", 8089)
        }

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
                get(urlEqualTo("/api/companies"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                            [
                              {"companyId": 789, "companyName": "OSCIN SARL"},
                              {"companyId": 790, "companyName": "Blanc SA"},
                              {"companyId": 791, "companyName": "TechStart SAS"}
                            ]
                        """.trimIndent()
                                        )
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
                event is ListOfCompaniesFetchedEvent &&
                        event.listOfCompanies.any { it.companyName == "OSCIN SARL" } &&
                        event.listOfCompanies.size == 3
            }
        }

        // Deep assertion
        streamAssertions.assertEvent(settingsId.toString()) { event ->
            val fetchedEvent = event as ListOfCompaniesFetchedEvent
            assertThat(fetchedEvent.listOfCompanies)
                    .extracting("companyName")
                    .containsExactlyInAnyOrder("OSCIN SARL", "Blanc SA", "TechStart SAS")
            true
        }
    }
}
