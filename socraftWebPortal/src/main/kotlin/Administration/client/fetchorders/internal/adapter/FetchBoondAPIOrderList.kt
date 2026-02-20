package administration.client.fetchorders.internal.adapter

import administration.common.OrderDetails
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

data class OrdersListInfo(val orders: List<OrderDetails>)

data class BoondOrdersResponse(
        val data: List<BoondOrderData>,
        val included: List<BoondIncludedData>? = null
)

data class BoondOrderData(
        val id: Long,
        val attributes: BoondOrderAttributes,
        val relationships: BoondOrderRelationships
)

data class BoondOrderAttributes(
        val companyId: Long,
        val projectId: Long,
        val reference: String? = null,
        val title: String,
        val state: Int? = null,
        val orderDate: String? = null,
        val startDate: String? = null,
        val endDate: String? = null,
        val informations: BoondOrderInformations? = null
)

data class BoondOrderInformations(
        val totalExcludingTaxes: Double? = null,
        val totalIncludingTaxes: Double? = null,
        val totalVat: Double? = null
)

data class BoondOrderRelationships(
        val company: CompanyRelation? = null,
        val project: ProjectRelation? = null
)

data class CompanyRelation(val data: CompanyData?)

data class CompanyData(val id: Long)

data class ProjectRelation(val data: ProjectData?)

data class ProjectData(val id: Long, val reference: String? = null)

data class BoondIncludedData(
        val id: String,
        val typeResource: String,
        val attributes: Map<String, Any>
)

@Component
class FetchBoondAPIOrderList(
        @Value("\${administration.api.base-url}") private val baseUrl: String,
        @Value("\${administration.api.token}") private val apiToken: String
) {
  private val logger = KotlinLogging.logger {}
  private val restTemplate = RestTemplate()

  fun fetch(companyId: Long, projectReference: String? = null): OrdersListInfo {
    logger.info("Fetching orders for companyId: $companyId, projectReference: $projectReference")

    val headers =
            HttpHeaders().apply {
              set("Authorization", apiToken)
              accept = listOf(MediaType.APPLICATION_JSON)
            }

    // Build URL with query parameters
    val urlBuilder =
            UriComponentsBuilder.fromUriString("$baseUrl/orders")
                    .queryParam("companies", companyId)
                    .queryParam(
                            "fields[orders]",
                            "reference,title,state,orderDate,startDate,endDate,informations"
                    )

    // Add project filter if provided
    if (projectReference != null) {
      urlBuilder.queryParam("projects", projectReference)
    }

    val url = urlBuilder.toUriString()

    logger.info("Request URL: $url")

    return try {
      val response =
              restTemplate.exchange(
                      url,
                      HttpMethod.GET,
                      HttpEntity<Any>(headers),
                      BoondOrdersResponse::class.java
              )

      logger.info("Response received. Total orders in response: ${response.body?.data?.size ?: 0}")

      // Log all order IDs and their company/project IDs
      response.body?.data?.forEach { order ->
        logger.info(
                "Order ${order.id} (${order.attributes.reference}) - Company: ${order.relationships.company?.data?.id}, Project: ${order.relationships.project?.data?.reference}"
        )
      }

      val allOrders = response.body?.data ?: emptyList()

      // DOUBLE-CHECK: Filter by companyId and optionally projectReference
      val filteredOrders =
              allOrders.filter { order ->
                val matchesCompany = order.relationships.company?.data?.id == companyId
                val matchesProject =
                        projectReference == null ||
                                order.relationships.project?.data?.reference == projectReference
                matchesCompany && matchesProject
              }

      logger.info(
              "Orders after filtering: ${filteredOrders.size} (filtered out ${allOrders.size - filteredOrders.size} orders)"
      )

      val orders =
              filteredOrders.map {
                logger.info("Mapping order ${it.attributes.reference}")

                OrderDetails(
                        orderId = it.id,
                        companyId = it.relationships.company?.data?.id ?: 0,
                        projectId = it.relationships.project?.data?.id ?: 0,
                        reference = it.attributes.reference ?: it.id.toString(),
                        title = it.attributes.title,
                        state = it.attributes.state ?: 0,
                        orderDate = it.attributes.orderDate ?: "",
                        startDate = it.attributes.startDate ?: "",
                        endDate = it.attributes.endDate ?: "",
                        // FLATTENED MAPPING: No more 'informations =
                        // Informations(...)'
                        totalExcludingTaxes = it.attributes.informations?.totalExcludingTaxes
                                        ?: 0.0,
                        totalIncludingTaxes = it.attributes.informations?.totalIncludingTaxes
                                        ?: 0.0,
                        totalVat = it.attributes.informations?.totalVat ?: 0.0
                )
              }

      logger.info("Successfully fetched ${orders.size} orders for companyId: $companyId")
      OrdersListInfo(orders)
    } catch (ex: Exception) {
      logger.error("Error fetching orders: ${ex.message}", ex)
      OrdersListInfo(emptyList())
    }
  }
}
