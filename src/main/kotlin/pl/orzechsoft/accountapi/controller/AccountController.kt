package pl.orzechsoft.accountapi.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException
import pl.orzechsoft.accountapi.model.Account
import pl.orzechsoft.accountapi.model.Balance
import pl.orzechsoft.accountapi.service.AccountService
import reactor.core.publisher.Mono
import java.util.*

@Controller
class AccountController(private val accountService: AccountService) {

    @GetMapping(
        path = ["/account/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun getAccount(@PathVariable("id") id: UUID): Mono<Account> {
        return accountService.getById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
    }

    @GetMapping(
        path = ["/account/{id}/balance/{currencyCode}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun getAccountBalance(
        @PathVariable("id") id: UUID,
        @PathVariable("currencyCode") currencyCode: String
    ): Mono<Balance> {
        return accountService.getBalanceById(id, currencyCode)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
    }
}