package pl.orzechsoft.accountapi.model

import java.math.BigDecimal

data class Balance(
    val balancePln: BigDecimal,
    val balanceCurrency: BigDecimal,
    val currencyCode: String
)
