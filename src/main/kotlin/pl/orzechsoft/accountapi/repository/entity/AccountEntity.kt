package pl.orzechsoft.accountapi.repository.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.*

@Table("account")
data class AccountEntity(@Id val dbId: Long?, val accountId: UUID, val balancePln: BigDecimal)