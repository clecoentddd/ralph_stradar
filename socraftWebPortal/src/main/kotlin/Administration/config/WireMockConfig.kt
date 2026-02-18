// src/main/kotlin/administration/config/WireMockConfig.kt
package administration.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ResourceLoader

@Configuration
@Profile("dev", "test")
class WireMockConfig(
        private val resourceLoader: ResourceLoader,
        @Value("\${wiremock.port:8089}") private val port: Int
) {

        private val logger = KotlinLogging.logger {}
        private lateinit var wireMockServer: WireMockServer

        @PostConstruct
        fun startWireMock() {
                logger.info("Starting WireMock server on port $port")

                wireMockServer =
                        WireMockServer(
                                WireMockConfiguration.options()
                                        .port(port)
                                        .usingFilesUnderClasspath(
                                                "wiremock"
                                        ) // Use src/main/resources/wiremock/
                        )

                wireMockServer.start()

                // Configure stubs
                setupBoondAPIStubs()

                logger.info("WireMock server started successfully at http://localhost:$port")
        }

        @PreDestroy
        fun stopWireMock() {
                logger.info("Stopping WireMock server")
                wireMockServer.stop()
        }

        private fun setupBoondAPIStubs() {

                // Companies list endpoint
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/companies"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/companies.json")
                                )
                )

                // Projects endpoint - Company 789 (2 projects)
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/projects"))
                                .withQueryParam("companies", WireMock.equalTo("789"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/projects-789.json")
                                )
                )

                // Projects endpoint - Company 790 (3 projects)
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/projects"))
                                .withQueryParam("companies", WireMock.equalTo("790"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/projects-790.json")
                                )
                )

                // Projects endpoint - Company 791 (4 projects)
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/projects"))
                                .withQueryParam("companies", WireMock.equalTo("791"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/projects-791.json")
                                )
                )

                // Invoices endpoint
                // Invoices endpoint - Company 789
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/invoices"))
                                .withQueryParam("companies", WireMock.equalTo("789"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/invoices-789.json")
                                )
                )

                // Invoices endpoint - Company 790
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/invoices"))
                                .withQueryParam("companies", WireMock.equalTo("790"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/invoices-790.json")
                                )
                )

                // Invoices endpoint - Company 791
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/invoices"))
                                .withQueryParam("companies", WireMock.equalTo("791"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/invoices-791.json")
                                )
                )

                // Orders endpoint
                // Orders endpoint - Company 789
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/orders"))
                                .withQueryParam("companies", WireMock.equalTo("789"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile(" /orders-789.json")
                                )
                )

                // Orders endpoint - Company 790
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/orders"))
                                .withQueryParam("companies", WireMock.equalTo("790"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/orders-790.json")
                                )
                )

                // Orders endpoint - Company 791
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/orders"))
                                .withQueryParam("companies", WireMock.equalTo("791"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/orders-791.json")
                                )
                )

                // Invoice states mapping endpoint
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlPathEqualTo("/api/v1/admin/invoices/states"))
                                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBodyFile("administration/invoice-states.json")
                                )
                )

                // Missing token error
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlMatching("/api/v1/.*"))
                                .withHeader("Authorization", WireMock.absent())
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(401)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody(
                                                        """{"error": "Unauthorized", "message": "Missing authorization token"}"""
                                                )
                                )
                )

                // Invalid token error
                wireMockServer.stubFor(
                        WireMock.get(WireMock.urlMatching("/api/v1/.*"))
                                .withHeader(
                                        "Authorization",
                                        WireMock.equalTo("Bearer invalid_token")
                                )
                                .willReturn(
                                        WireMock.aResponse()
                                                .withStatus(403)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody(
                                                        """{"error": "Forbidden", "message": "Invalid token"}"""
                                                )
                                )
                )

                logger.info("WireMock stubs configured for Boond API endpoints")
        }

        @Bean
        fun getWireMockServer(): WireMockServer {
                return wireMockServer
        }
}
