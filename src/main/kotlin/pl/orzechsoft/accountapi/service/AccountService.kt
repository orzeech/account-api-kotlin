package pl.orzechsoft.accountapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import pl.orzechsoft.accountapi.model.Account
import pl.orzechsoft.accountapi.model.Balance
import pl.orzechsoft.accountapi.model.nbp.CurrencyInfo
import pl.orzechsoft.accountapi.model.nbp.RateInfo
import pl.orzechsoft.accountapi.repository.AccountRepository
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Service
class AccountService(
    private val repository: AccountRepository,
    private val exchangeRateClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Real world example would use something better
    private var rateInfoCache: MutableMap<String, Pair<LocalDate, RateInfo?>> = mutableMapOf()

    fun getById(id: UUID): Mono<Account> {
        logger.info("Getting account by Id {}", id)
        return repository.findById(id).map { account -> account }
    }

    fun getBalanceById(id: UUID, currencyCode: String): Mono<Balance> {
        logger.info("Getting account balance for Id: {}", id)
        return repository.findById(id)
            .flatMap { account ->
                getExchangeRate(currencyCode).map { currencyRate ->
                    Balance(
                        account!!.balancePln,
                        account.balancePln.div(BigDecimal.valueOf(currencyRate.mid)),
                        currencyCode
                    )
                }
            }
    }


    private fun getExchangeRate(currencyCode: String): Mono<RateInfo> {
        // Disregarding timezones etc.
        if (!rateInfoCache.contains(currencyCode) || LocalDate.now().atStartOfDay()
                .isAfter(rateInfoCache[currencyCode]!!.first.atStartOfDay())
        ) {
            logger.info("Returning RateInfo from API")
            return exchangeRateClient.get().uri("/${currencyCode}?format=json")
                .exchangeToMono { response ->
                    response.bodyToMono(CurrencyInfo::class.java)
                        .map { currencyInfo -> currencyInfo.rates[0] }
                        .doOnNext { rateInfo ->
                            rateInfoCache[currencyCode] =
                                Pair(LocalDate.now(), rateInfo)
                        }
                }
        }
        logger.info("Getting RateInfo from Cache")
        return Mono.just(rateInfoCache[currencyCode]!!.second!!)
    }
}