package administration.common

data class ListOfProjectsItem(
        var projectId: Long = 0,
        var reference: String? = null,
        var projectTitle: String? = null,
        var projectDescription: String? = null,
        var startDate: String? = null,
        var endDate: String? = null,
        var forecastEndDate: String? = null,
        var status: String? = null,
        var manager: String? = null
)
