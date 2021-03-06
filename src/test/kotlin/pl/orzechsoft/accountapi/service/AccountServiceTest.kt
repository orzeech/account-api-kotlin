package pl.orzechsoft.accountapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.client.WebClient
import pl.orzechsoft.accountapi.model.Account
import pl.orzechsoft.accountapi.model.Balance
import pl.orzechsoft.accountapi.model.nbp.CurrencyInfo
import pl.orzechsoft.accountapi.model.nbp.RateInfo
import pl.orzechsoft.accountapi.repository.MapAccountRepository
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit


internal class AccountServiceTest {

    private val currency = "USD"
    lateinit var service: AccountService
    lateinit var repository: MapAccountRepository

    companion object {
        lateinit var mockBackEnd: MockWebServer

        @BeforeAll
        @JvmStatic
        internal fun startMockWebServer() {
            mockBackEnd = MockWebServer()
            mockBackEnd.start()
        }

        @AfterAll
        @JvmStatic
        internal fun shutdownMockWebServer() {
            mockBackEnd.shutdown()
        }
    }

    @BeforeEach
    fun initTest() {
        repository = Mockito.mock(MapAccountRepository::class.java)
        val webClient = WebClient.builder()
            .baseUrl("http://localhost:${mockBackEnd.port}")
            .build()
        service = AccountService(repository, webClient)
    }

    @Test
    @DisplayName("Should return account")
    fun shouldReturnAccount() {
        val id = UUID.randomUUID()
        val account = Account(id, BigDecimal(43.4))
        Mockito.`when`(repository.findById(id)).thenReturn(Mono.just(account))
        StepVerifier.create(service.getById(id))
            .expectNext(account)
            .verifyComplete()
        verify(repository, times(1)).findById(id)
    }

    @Test
    @DisplayName("Should return balance")
    fun shouldReturnBalance() {
        val id = UUID.randomUUID()
        val balancePln = BigDecimal(342.23)
        val account = Account(id, balancePln)
        val exchangeRate = 3.56
        val balance = Balance(balancePln, balancePln.div(BigDecimal(exchangeRate)), currency)
        val currencyInfo = CurrencyInfo(
            "A", "Dolan amerykański", currency, listOf(
                RateInfo("some/no", "2021-03-06", exchangeRate)
            )
        )

        val objectMapper = ObjectMapper().registerKotlinModule()
        Mockito.`when`(repository.findById(id)).thenReturn(Mono.just(account))
        mockBackEnd.enqueue(
            MockResponse().setResponseCode(HttpStatus.OK.value())
                .setBody(objectMapper.writeValueAsString(currencyInfo))
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
        )
        StepVerifier.create(service.getBalanceById(id, currency))
            .expectNextMatches { bal ->
                bal.balancePln == balance.balancePln && bal.currencyCode == currency && bal.balanceCurrency.minus(
                    balance.balanceCurrency
                ).abs() < BigDecimal(0.000001)
            }
            .verifyComplete()
        verify(repository, times(1)).findById(id)
        mockBackEnd.takeRequest(5, TimeUnit.SECONDS)
    }

    @Test
    @DisplayName("Should cache rate info object")
    fun shouldCacheRateInfo() {
        val id = UUID.randomUUID()
        val balancePln = BigDecimal(342.23)
        val account = Account(id, balancePln)
        val exchangeRate = 3.56
        val currencyInfo = CurrencyInfo(
            "A", "Dolan amerykański", currency, listOf(
                RateInfo("some/no", "2021-03-06", exchangeRate)
            )
        )

        val objectMapper = ObjectMapper().registerKotlinModule()
        Mockito.`when`(repository.findById(id)).thenReturn(Mono.just(account))
        mockBackEnd.enqueue(
            MockResponse().setResponseCode(HttpStatus.OK.value())
                .setBody(objectMapper.writeValueAsString(currencyInfo))
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
        )
        mockBackEnd.enqueue(
            MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
        )
        for (i in 0..4) {
            StepVerifier.create(service.getBalanceById(id, currency))
                .expectNextCount(1)
                .verifyComplete()
        }

        verify(repository, times(5)).findById(id)
        // Assert that mock back end was called only once
        assertEquals("/$currency?format=json", mockBackEnd.takeRequest(5, TimeUnit.SECONDS)!!.path)
        assertNull(mockBackEnd.takeRequest(1, TimeUnit.SECONDS))
    }
}