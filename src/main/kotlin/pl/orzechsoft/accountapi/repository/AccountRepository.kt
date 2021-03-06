package pl.orzechsoft.accountapi.repository

import pl.orzechsoft.accountapi.model.Account
import reactor.core.publisher.Mono
import java.util.*

interface AccountRepository {

    fun findById(id: UUID): Mono<Account>
}