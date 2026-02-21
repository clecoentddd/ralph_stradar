package administration.admin.adminconnection.fetchcompanieslist.integration

import administration.admin.domain.commands.initializesettings.CreateSettingsCommand
import administration.admin.domain.commands.requestcompanylistupdate.RequestCompanyListUpdateCommand
import administration.common.support.BaseIntegrationTest
import administration.common.support.RandomData
import administration.common.support.StreamAssertions
import administration.common.support.awaitUntilAssserted
import administration.events.ListOfCompaniesFetchedEvent
import administration.support.metadata.AdminSecurityHeaders
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import java.util.*
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

class FetchCompaniesListBoondDataCheckTest : BaseIntegrationTest() {

  @Autowired private lateinit var commandGateway: CommandGateway
  @Autowired private lateinit var streamAssertions: StreamAssertions

  companion object {
    private val wireMockServer =
            WireMockServer(wireMockConfig().port(8089).usingFilesUnderClasspath("wiremock"))

    @BeforeAll
    @JvmStatic
    fun startWireMock() {
      if (!wireMockServer.isRunning) {
        try {
          wireMockServer.start()
          WireMock.configureFor("localhost", 8089)
          println("WireMock started on port 8089")
        } catch (e: Exception) {
          println(
                  "WireMock failed to start (maybe port 8089 is taken by another app?): ${e.message}"
          )
          // If it's already running by another process, WireMock.configureFor might still work
          WireMock.configureFor("localhost", 8089)
        }
      }
    }

    @AfterAll
    @JvmStatic
    fun stopWireMock() {
      // Check if it's running BEFORE trying to stop it
      if (wireMockServer.isRunning) {
        try {
          wireMockServer.stop()
        } catch (e: Exception) {
          println("Skipping WireMock stop: ${e.message}")
        }
      } else {
        println("WireMock was already stopped or managed externally. Skipping stop.")
      }
    }
  }

  @BeforeEach
  fun clearMappings() {
    WireMock.reset()
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
                                    .withBodyFile("administration/companies.json")
                    )
    )

    // 2. Setup IDs
    val settingsId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val connectionId = UUID.randomUUID()

    // Create common Metadata for the test to satisfy the Interceptor
    val testMetadata =
            MetaData.with(AdminSecurityHeaders.SESSION_ID, "test-session")
                    .and(AdminSecurityHeaders.ADMIN_COMPANY_ID, "test-company")

    // 3. Create Settings (Wrapped with Metadata)
    val createSettingsCommand =
            RandomData.newInstance<CreateSettingsCommand> {
              this.settingsId = settingsId
              this.connectionId = connectionId
            }

    commandGateway.sendAndWait<Any>(
            GenericCommandMessage.asCommandMessage<CreateSettingsCommand>(createSettingsCommand)
                    .withMetaData(testMetadata)
    )

    // 4. Request Update (Wrapped with Metadata)
    val requestUpdateCommand =
            RandomData.newInstance<RequestCompanyListUpdateCommand> { this.settingsId = settingsId }

    commandGateway.sendAndWait<Any>(
            GenericCommandMessage.asCommandMessage<RequestCompanyListUpdateCommand>(
                            requestUpdateCommand
                    )
                    .withMetaData(testMetadata)
    )

    // 5. Verify Event
    awaitUntilAssserted {
      streamAssertions.assertEvent(settingsId.toString()) { event ->
        event is ListOfCompaniesFetchedEvent &&
                event.listOfCompanies.size == 3 &&
                event.listOfCompanies.any { it.companyName == "OSCIN SARL" }
      }
    }

    // Deep assertion
    awaitUntilAssserted {
      streamAssertions.assertEvent(settingsId.toString()) { event ->
        event is ListOfCompaniesFetchedEvent &&
                event.listOfCompanies.size == 3 &&
                event.listOfCompanies.any { it.companyName == "OSCIN SARL" }
      }
    }
  }
}
