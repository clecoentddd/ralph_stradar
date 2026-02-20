package administration.admin.fetchinvoicestatemappinglist.internal.adapter

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

// --- Domain & DTO Classes ---

data class invoiceStateMapping(val code: Long, val label: String)

data class InvoiceStateMappingInfo(val states: List<invoiceStateMapping>)

// --- JSON Mapping Classes ---

data class BoondInvoiceStatesResponse(val data: List<BoondInvoiceStateData>)

data class BoondInvoiceStateData(val code: Long, val label: String)

// --- The Adapter ---

@Component
class FetchBoondAPIInvoiceStateMapping(
    @Value("\${administration.api.base-url}") private val baseUrl: String,
    @Value("\${administration.api.token}") private val apiToken: String
) {
  private val logger = KotlinLogging.logger {}
  private val restTemplate = RestTemplate()

  fun fetchAll(): InvoiceStateMappingInfo {
    logger.info("Fetching invoice state mapping from Boond API")

    val headers =
        HttpHeaders().apply {
          set("Authorization", apiToken)
          accept = listOf(MediaType.APPLICATION_JSON)
        }

    val url = UriComponentsBuilder.fromUriString("$baseUrl/admin/invoices/states").toUriString()

    logger.info("Request URL: $url")

    return try {
      val response =
          restTemplate.exchange(
              url, HttpMethod.GET, HttpEntity<Any>(headers), BoondInvoiceStatesResponse::class.java)

      val states =
          response.body?.data?.map { invoiceStateMapping(code = it.code, label = it.label) }
              ?: emptyList()

      logger.info("Fetched ${states.size} invoice states")
      InvoiceStateMappingInfo(states)
    } catch (ex: Exception) {
      logger.error("Error fetching invoice states: ${ex.message}", ex)
      InvoiceStateMappingInfo(emptyList())
    }
  }

  fun getLabelByCode(code: Long): String? {
    val mappingInfo = fetchAll()
    return mappingInfo.states.find { it.code == code }?.label
  }
}
