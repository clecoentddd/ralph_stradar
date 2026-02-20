package administration.client.fetchprojects.internal.adapter

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

data class Project(
    val projectId: Long, // mapped from reference
    val reference: String, // mapped from reference
    val projectTitle: String, // mapped from title
    val projectDescription: String,
    val startDate: String?,
    val endDate: String?,
    val forecastEndDate: String?,
    val status: String,
    val manager: String? = null,
    val companyId: Long? = null
)

data class Manager(val firstName: String, val lastName: String, val email: String)

data class ProjectsListInfo(val projects: List<Project>)

data class BoondProjectsResponse(
    val data: List<BoondProjectData>,
    val included: List<BoondIncludedData>? = null
)

data class BoondProjectData(
    val id: Long,
    val attributes: BoondProjectAttributes,
    val relationships: BoondProjectRelationships
)

data class BoondProjectAttributes(
    val reference: String? = null,
    val title: String,
    val description: String? = null,
    val state: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val forecastEndDate: String? = null
)

data class BoondProjectRelationships(
    val company: CompanyRelation? = null,
    val contact: ContactRelation? = null,
    val manager: ManagerRelation? = null
)

data class ManagerRelation(val data: ManagerData?)

data class ManagerData(val id: String, val typeResource: String)

data class ContactRelation(val data: ContactData?)

data class ContactData(val id: Long)

data class CompanyRelation(val data: CompanyData)

data class CompanyData(val id: Long)

data class BoondIncludedData(
    val id: String,
    val typeResource: String,
    val attributes: Map<String, Any>
)

@Component
class FetchBoondAPIProjectList(
    @Value("\${administration.api.base-url}") private val baseUrl: String,
    @Value("\${administration.api.token}") private val apiToken: String
) {
  private val logger = KotlinLogging.logger {}
  private val restTemplate = RestTemplate()

  fun fetch(companyId: Long): ProjectsListInfo {
    logger.info("Fetching projects for companyId: $companyId")

    val headers =
        HttpHeaders().apply {
          set("Authorization", apiToken)
          accept = listOf(MediaType.APPLICATION_JSON)
        }

    // Build URL with query parameters
    val url =
        UriComponentsBuilder.fromUriString("$baseUrl/projects")
            .queryParam("companies", companyId)
            .queryParam(
                "fields[projects]",
                "reference,title,description,startDate,endDate,forecastEndDate,state")
            .queryParam("include", "manager")
            .queryParam("fields[users]", "firstName,lastName,email")
            .toUriString()

    logger.info("Request URL: $url")

    return try {
      val response =
          restTemplate.exchange(
              url, HttpMethod.GET, HttpEntity<Any>(headers), BoondProjectsResponse::class.java)

      logger.info(
          "Response received. Total projects in response: ${response.body?.data?.size ?: 0}")

      // Log all project IDs and their company IDs
      response.body?.data?.forEach { project ->
        logger.info(
            "Project ${project.id} (${project.attributes.reference}) belongs to company ${project.relationships.company?.data?.id}")
      }

      // Build a map of managers by ID for easy lookup
      val managersById =
          response.body
              ?.included
              ?.filter { it.typeResource == "users" }
              ?.associate {
                it.id to
                    Manager(
                        firstName = it.attributes["firstName"] as? String ?: "",
                        lastName = it.attributes["lastName"] as? String ?: "",
                        email = it.attributes["email"] as? String ?: "")
              } ?: emptyMap()

      logger.info("Managers loaded: ${managersById.keys.joinToString()}")

      val allProjects = response.body?.data ?: emptyList()

      // DOUBLE-CHECK: Filter by companyId just in case the API returned all
      // projects
      val filteredProjects = allProjects.filter { it.relationships.company?.data?.id == companyId }

      logger.info(
          "Projects after filtering: ${filteredProjects.size} (filtered out ${allProjects.size - filteredProjects.size} projects)")

      val projects =
          filteredProjects.map {
            val managerId = it.relationships.manager?.data?.id
            val manager = managerId?.let { id -> managersById[id] }

            // Combine first and last name into a single string
            val managerFullName =
                manager?.let { m -> "${m.firstName} ${m.lastName}".trim() } ?: "Unknown Manager"

            logger.info(
                "Mapping project ${it.attributes.reference} with manager: ${manager?.firstName} ${manager?.lastName}")

            Project(
                projectId = it.id,
                reference = it.attributes.reference ?: "",
                projectTitle = it.attributes.title,
                projectDescription = it.attributes.description ?: "",
                startDate = it.attributes.startDate,
                endDate = it.attributes.endDate,
                forecastEndDate = it.attributes.forecastEndDate,
                status = it.attributes.state?.toString() ?: "unknown",
                manager = managerFullName,
                companyId = it.relationships.company?.data?.id)
          }

      logger.info("Successfully fetched ${projects.size} projects for companyId: $companyId")
      ProjectsListInfo(projects)
    } catch (ex: Exception) {
      logger.error("Error fetching projects: ${ex.message}", ex)
      ProjectsListInfo(emptyList())
    }
  }
}
