package pl.orzechsoft.accountapi.repository

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import pl.orzechsoft.accountapi.model.Account
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.random.Random

@Repository
@Profile("map")
class MapAccountRepository : AccountRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val db: Map<UUID, Account> = initAccounts()

    private fun initAccounts(): Map<UUID, Account> {
        val map = mutableMapOf<UUID, Account>()
        val random = Random(System.nanoTime())
        for (i in 0..10) {
            val uuid = UUID.randomUUID()
            val balancePln = BigDecimal.valueOf(random.nextDouble(15000.0))
                .setScale(2, RoundingMode.HALF_DOWN)
            map[uuid] = Account(uuid, balancePln)
            logger.info("Initialized account $uuid with balance: $balancePln")
        }
        return map
    }

    override fun findById(id: UUID): Mono<Account> {
        return Mono.justOrEmpty(db[id])
    }
}