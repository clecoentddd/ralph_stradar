package administration.client.fetchinvoices.internal.adapter

import administration.common.ListOfInvoicesItem
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

data class InvoicesListInfo(val invoices: List<ListOfInvoicesItem>)

data class BoondInvoicesResponse(
        val data: List<BoondInvoiceData>,
)

data class BoondInvoiceData(
        val id: Long,
        val attributes: BoondInvoiceAttributes,
        val relationships: BoondInvoiceRelationships
)

data class BoondInvoiceAttributes(
        val reference: String? = null,
        val title: String,
        val state: Int? = null,
        val invoiceDate: String? = null,
        val dueDate: String? = null,
        val paymentDate: String? = null,
        val performedDate: String? = null,
        val amount: Double? = null,
        val informations: BoondListOfInvoicesItemrmations? = null
)

data class BoondListOfInvoicesItemrmations(
        val totalExcludingTaxes: Double? = null,
        val totalIncludingTaxes: Double? = null,
        val totalVat: Double? = null
)

data class BoondInvoiceRelationships(
        val company: InvoiceCompanyRelation? = null,
        val project: InvoiceProjectRelation? = null,
        val order: InvoiceOrderRelation? = null
)

data class InvoiceCompanyRelation(val data: InvoiceCompanyData?)

data class InvoiceCompanyData(val id: Long)

data class InvoiceProjectRelation(val data: InvoiceProjectData?)

data class InvoiceProjectData(val id: Long, val reference: String? = null)

data class InvoiceOrderRelation(val data: InvoiceOrderData?)

data class InvoiceOrderData(val id: Long, val reference: String? = null)

data class InvoiceBoondIncludedData(
        val id: String,
        val typeResource: String,
        val attributes: Map<String, Any>
)

@Component
class FetchBoondAPIInvoiceList(
        @Value("\${administration.api.base-url}") private val baseUrl: String,
        @Value("\${administration.api.token}") private val apiToken: String
) {
        private val logger = KotlinLogging.logger {}
        private val restTemplate = RestTemplate()

        fun fetch(
                companyId: Long,
                projectReference: String? = null,
                orderReference: String? = null
        ): InvoicesListInfo {
                logger.info(
                        "Fetching invoices for companyId: $companyId, projectReference: $projectReference, orderReference: $orderReference"
                )

                val headers =
                        HttpHeaders().apply {
                                set("Authorization", apiToken)
                                accept = listOf(MediaType.APPLICATION_JSON)
                        }

                // Build URL with query parameters
                val urlBuilder =
                        UriComponentsBuilder.fromUriString("$baseUrl/invoices")
                                .queryParam("companies", companyId)
                                .queryParam(
                                        "fields[invoices]",
                                        "reference,title,state,invoiceDate,dueDate,paymentDate,amount,informations"
                                )

                // Add project filter if provided
                if (projectReference != null) {
                        urlBuilder.queryParam("projects", projectReference)
                }

                // Add order filter if provided
                if (orderReference != null) {
                        urlBuilder.queryParam("orders", orderReference)
                }

                val url = urlBuilder.toUriString()

                logger.info("Request URL: $url")

                return try {
                        val response =
                                restTemplate.exchange(
                                        url,
                                        HttpMethod.GET,
                                        HttpEntity<Any>(headers),
                                        BoondInvoicesResponse::class.java
                                )

                        logger.info(
                                "Response received. Total invoices in response: ${response.body?.data?.size ?: 0}"
                        )

                        // Log all invoice IDs and their company/project/order IDs
                        response.body?.data?.forEach { invoice ->
                                logger.info(
                                        "Invoice ${invoice.id} (${invoice.attributes.reference}) - Company: ${invoice.relationships.company?.data?.id}, Project: ${invoice.relationships.project?.data?.reference}, Order: ${invoice.relationships.order?.data?.reference}"
                                )
                        }

                        val allInvoices = response.body?.data ?: emptyList()

                        // DOUBLE-CHECK: Filter by companyId and optionally projectReference and
                        // orderReference
                        val filteredInvoices =
                                allInvoices.filter { invoice ->
                                        val matchesCompany =
                                                invoice.relationships.company?.data?.id == companyId
                                        val matchesProject =
                                                projectReference == null ||
                                                        invoice.relationships
                                                                .project
                                                                ?.data
                                                                ?.reference == projectReference
                                        val matchesOrder =
                                                orderReference == null ||
                                                        invoice.relationships
                                                                .order
                                                                ?.data
                                                                ?.reference == orderReference
                                        matchesCompany && matchesProject && matchesOrder
                                }

                        logger.info(
                                "Invoices after filtering: ${filteredInvoices.size} (filtered out ${allInvoices.size - filteredInvoices.size} invoices)"
                        )

                        val invoices =
                                filteredInvoices.map {
                                        logger.info("Mapping invoice ${it.attributes.reference}")

                                        ListOfInvoicesItem(
                                                invoiceId = it.id,
                                                companyId = it.relationships.company?.data?.id ?: 0,
                                                projectId = it.relationships.project?.data?.id ?: 0,
                                                orderId = it.relationships.order?.data?.id ?: 0,
                                                reference = it.attributes.reference
                                                                ?: it.id.toString(),
                                                title = it.attributes.title,
                                                state = it.attributes.state ?: 0,
                                                invoiceDate = it.attributes.invoiceDate ?: "",
                                                dueDate = it.attributes.dueDate ?: "",
                                                performedDate = it.attributes.performedDate ?: "",
                                                totalExcludingTaxes =
                                                        it.attributes
                                                                .informations
                                                                ?.totalExcludingTaxes
                                                                ?: 0.0,
                                                totalIncludingTaxes =
                                                        it.attributes
                                                                .informations
                                                                ?.totalIncludingTaxes
                                                                ?: 0.0,
                                                totalVat = it.attributes.informations?.totalVat
                                                                ?: 0.0
                                        )
                                }

                        logger.info(
                                "Successfully fetched ${invoices.size} invoices for companyId: $companyId"
                        )
                        InvoicesListInfo(invoices)
                } catch (ex: Exception) {
                        logger.error("Error fetching invoices: ${ex.message}", ex)
                        InvoicesListInfo(emptyList())
                }
        }
}
