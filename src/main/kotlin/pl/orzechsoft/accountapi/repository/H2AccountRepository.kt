package pl.orzechsoft.accountapi.repository

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import pl.orzechsoft.accountapi.model.Account
import pl.orzechsoft.accountapi.repository.entity.AccountEntity
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import javax.annotation.PostConstruct
import kotlin.random.Random


@Repository
@Profile("h2")
class H2AccountRepository(val rdbcRepository: AccountRDBCRepository) : AccountRepository {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun findById(id: UUID): Mono<Account> {
        return rdbcRepository.findById(id)
            .map { accountEntity -> Account(accountEntity!!.accountId, accountEntity.balancePln) }
    }

    @PostConstruct
    fun init() {
        val random = Random(System.nanoTime())
        for (i in 0..5) {
            val uuid = UUID.randomUUID()
            val balancePln = BigDecimal.valueOf(random.nextDouble(15000.0))
                .setScale(2, RoundingMode.HALF_DOWN)
            rdbcRepository.save(AccountEntity(null, uuid, balancePln)).subscribe()
            logger.info("Initialized account $uuid with balance: $balancePln")
        }
    }
}