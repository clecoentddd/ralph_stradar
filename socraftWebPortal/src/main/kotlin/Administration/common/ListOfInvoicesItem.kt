package administration.common

import java.io.Serializable

data class ListOfInvoicesItem(
        var invoiceId: Long = 0,
        var companyId: Long = 0,
        var projectId: Long = 0,
        var orderId: Long = 0,
        var reference: String = "",
        var title: String = "",
        var state: Int = 0,
        var invoiceDate: String = "",
        var dueDate: String = "",
        var performedDate: String = "",
        // Flattened fields from "Informations"
        var totalExcludingTaxes: Double = 0.0,
        var totalIncludingTaxes: Double = 0.0,
        var totalVat: Double = 0.0
) : Serializable
