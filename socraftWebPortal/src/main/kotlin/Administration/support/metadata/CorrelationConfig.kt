package administration.support.metadata

import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val SESSION_ID_HEADER = "X-Session-Id"

@Configuration
class CorrelationConfig {
  @Bean
  fun adminCorrelationDataProvider(): CorrelationDataProvider {
    return SimpleCorrelationDataProvider(
        AdminSecurityHeaders.SESSION_ID, AdminSecurityHeaders.ADMIN_COMPANY_ID)
  }
}
