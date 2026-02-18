package administration.common

data class invoiceListItem(
        val invoiceId: Long = 0,
        val companyId: Long = 0,
        val projectId: Long = 0,
        val orderId: Long = 0,
        val reference: String = "",
        val title: String = "",
        val state: Long = 0,
        val creationDate: String = "",
        val invoiceDate: String = "",
        val dueDate: String = "",
        val paymentDate: String = "",
        val totalExcludingTaxes: Double = 0.0,
        val totalIncludingTaxes: Double = 0.0,
        val totalVat: Double = 0.0
)
