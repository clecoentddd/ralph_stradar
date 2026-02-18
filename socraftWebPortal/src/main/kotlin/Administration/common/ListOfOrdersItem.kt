package administration.common

data class ListOfOrdersItem(
        var orderId: Long,
        var companyId: Long,
        var projectId: Long,
        var reference: String,
        var title: String,
        var state: Int,
        var orderDate: String,
        var startDate: String,
        var endDate: String,
        // Flattened fields from "Informations"
        var totalExcludingTaxes: Double,
        var totalIncludingTaxes: Double,
        var totalVat: Double
)
