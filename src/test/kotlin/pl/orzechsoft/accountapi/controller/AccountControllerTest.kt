package pl.orzechsoft.accountapi.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pl.orzechsoft.accountapi.model.Account
import pl.orzechsoft.accountapi.model.Balance
import pl.orzechsoft.accountapi.service.AccountService
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.util.*

@SpringBootTest
internal class AccountControllerTest {

    @Autowired
    lateinit var controller: AccountController

    lateinit var client: WebTestClient

    @MockBean
    lateinit var service: AccountService

    @BeforeEach
    fun setup() {
        client = WebTestClient.bindToController(controller).build()
    }


    @Test
    @DisplayName("Should return account")
    fun shouldReturnAccount() {
        val id = UUID.randomUUID()
        val account = Account(id, BigDecimal(43.4))
        Mockito.`when`(service.getById(id)).thenReturn(Mono.just(account))
        client.get()
            .uri("/account/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody<Account>()
            .isEqualTo(account)
    }

    @Test
    @DisplayName("Should return 404 code when account is not found")
    fun shouldReturnNotFoundStatusCodeForAccountEndpoint() {
        val id = UUID.randomUUID()
        Mockito.`when`(service.getById(id)).thenReturn(Mono.empty())
        client.get()
            .uri("/account/$id")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .isEmpty
    }

    @Test
    @DisplayName("Should return 400 code when passed wrong Id")
    fun shouldReturnBadRequestStatusCodeForAccountEndpoint() {
        val id = "definitelynotuuid"
        client.get()
            .uri("/account/$id")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .isEmpty
    }

    @Test
    @DisplayName("Should return account balance")
    fun shouldReturnAccountBalance() {
        val id = UUID.randomUUID()
        val currencyCode = "USD"
        val balance = Balance(BigDecimal(341.4), BigDecimal(43.4), currencyCode)
        Mockito.`when`(service.getBalanceById(id, currencyCode)).thenReturn(Mono.just(balance))
        client.get()
            .uri("/account/$id/balance/$currencyCode")
            .exchange()
            .expectStatus().isOk
            .expectBody<Balance>()
            .isEqualTo(balance)
    }

    @Test
    @DisplayName("Balance endpoint should return 404 code when account is not found")
    fun shouldReturnNotFoundStatusCodeForBalanceEndpoint() {
        val id = UUID.randomUUID()
        val currencyCode = "USD"
        Mockito.`when`(service.getBalanceById(id, currencyCode)).thenReturn(Mono.empty())
        client.get()
            .uri("/account/$id/balance/$currencyCode")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .isEmpty
    }

    @Test
    @DisplayName("Should return 400 code when passed wrong Id to balance endpoint")
    fun shouldReturnBadRequestStatusCodeForBalanceEndpoint() {
        val id = "definitelynotuuid"
        client.get()
            .uri("/account/$id/balance")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .isEmpty
    }
}