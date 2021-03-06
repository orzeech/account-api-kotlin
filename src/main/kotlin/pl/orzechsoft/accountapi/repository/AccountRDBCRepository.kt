package pl.orzechsoft.accountapi.repository

import org.springframework.context.annotation.Profile
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import pl.orzechsoft.accountapi.repository.entity.AccountEntity
import reactor.core.publisher.Mono
import java.util.*

@Profile("h2")
interface AccountRDBCRepository : ReactiveCrudRepository<AccountEntity?, Long> {
    @Query("select account_id, balance_pln from account a where a.account_id = :id")
    fun findById(id: UUID?): Mono<AccountEntity?>
}