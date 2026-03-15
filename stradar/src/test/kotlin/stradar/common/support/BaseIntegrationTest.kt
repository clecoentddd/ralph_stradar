package stradar.common.support

import java.time.Duration
import org.awaitility.Awaitility
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@TestPropertySource(
    properties =
        [
            "auth0.domain=test-domain",
            "auth0.client-id=test-client-id",
            "auth0.client-secret=test-client-secret",
            "auth0.managementToken=test-token"])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext
abstract class BaseIntegrationTest {

  companion object {
    @org.testcontainers.junit.jupiter.Container
    private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres")).withReuse(true)

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { postgres.jdbcUrl }
      registry.add("spring.flyway.url") { postgres.jdbcUrl }
      registry.add("spring.datasource.username") { "test" }
      registry.add("spring.datasource.password") { "test" }
      registry.add("spring.flyway.user") { "test" }
      registry.add("spring.flyway.password") { "test" }
    }
  }
}

fun awaitUntilAssserted(fn: () -> Unit) {
  Awaitility.await().pollInSameThread().atMost(Duration.ofSeconds(15)).untilAsserted { fn() }
}
