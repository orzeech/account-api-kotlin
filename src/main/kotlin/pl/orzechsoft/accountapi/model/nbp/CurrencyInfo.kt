package pl.orzechsoft.accountapi.model.nbp

data class CurrencyInfo(
    val table: String,
    val currency: String,
    val code: String,
    val rates: List<RateInfo>
)

data class RateInfo(val no: String, val effectiveDate: String, val mid: Double)
