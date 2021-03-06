package pl.orzechsoft.accountapi.model

import java.math.BigDecimal
import java.util.*


data class Account(val id: UUID, val balancePln: BigDecimal)
