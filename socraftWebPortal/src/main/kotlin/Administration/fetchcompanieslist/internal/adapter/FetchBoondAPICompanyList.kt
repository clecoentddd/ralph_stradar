package administration.fetchcompanieslist.internal.adapter

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

// --- Domain & DTO Classes ---

data class Company(
        val companyId: Long,
        val companyName: String,
        val companyReference: String,
        val mainActivity: String,
        val city: String
)

data class CompaniesListInfo(val companies: List<Company>)

// --- JSON Mapping Classes ---

data class BoondCompaniesResponse(val data: List<BoondCompanyData>)

data class BoondCompanyData(
        val id: Long,
        val typeResource: String,
        val attributes: BoondCompanyAttributes
)

data class BoondCompanyAttributes(
        val reference: String,
        val name: String,
        val typeCompany: Int,
        val mainActivity: String,
        val address: BoondCompanyAddress,
        val contact: BoondCompanyContact,
        val siret: String,
        val state: Int,
        val creationDate: String,
        val informations: BoondCompanyInformations
)

data class BoondCompanyAddress(
        val street: String,
        val postcode: String,
        val city: String,
        val country: String
)

data class BoondCompanyContact(val phone: String, val email: String, val website: String)

data class BoondCompanyInformations(val revenue: Double, val employeesCount: Int)

// --- The Adapter ---

@Component
class FetchBoondAPICompanyList(
        @Value("\${administration.api.base-url}") private val baseUrl: String,
        @Value("\${administration.api.token}") private val apiToken: String
) {
  private val logger = KotlinLogging.logger {}
  private val restTemplate = RestTemplate()

  fun fetchAll(): CompaniesListInfo {
    logger.info("Fetching all companies from Boond API")

    val headers =
            HttpHeaders().apply {
              set("Authorization", apiToken)
              accept = listOf(MediaType.APPLICATION_JSON)
            }

    val url = "$baseUrl/companies"

    return try {
      val response =
              restTemplate.exchange(
                      url,
                      HttpMethod.GET,
                      HttpEntity<Any>(headers),
                      BoondCompaniesResponse::class.java
              )

      val companies =
              response.body?.data?.map {
                Company(
                        companyId = it.id,
                        companyName = it.attributes.name,
                        companyReference = it.attributes.reference,
                        mainActivity = it.attributes.mainActivity,
                        city = it.attributes.address.city
                )
              }
                      ?: emptyList()

      logger.info("Fetched ${companies.size} companies")
      CompaniesListInfo(companies)
    } catch (ex: Exception) {
      logger.error("Error fetching companies: ${ex.message}", ex)
      CompaniesListInfo(emptyList())
    }
  }

  fun fetchById(companyId: Long): Company? {
    logger.info("Fetching company $companyId from Boond API")

    val headers =
            HttpHeaders().apply {
              set("Authorization", apiToken)
              accept = listOf(MediaType.APPLICATION_JSON)
            }

    val url = "$baseUrl/companies/$companyId"

    return try {
      val response =
              restTemplate.exchange(
                      url,
                      HttpMethod.GET,
                      HttpEntity<Any>(headers),
                      BoondCompanyData::class.java
              )

      response.body?.let {
        Company(
                companyId = it.id,
                companyName = it.attributes.name,
                companyReference = it.attributes.reference,
                mainActivity = it.attributes.mainActivity,
                city = it.attributes.address.city
        )
      }
    } catch (ex: Exception) {
      logger.error("Error fetching company: ${ex.message}", ex)
      null
    }
  }
}
